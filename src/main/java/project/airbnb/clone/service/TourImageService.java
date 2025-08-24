package project.airbnb.clone.service;

import java.io.StringReader;
import java.net.URI;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import project.airbnb.clone.entity.*;
import project.airbnb.clone.repository.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourImageService {

    @Value("${tourapi.key}")
    private String tourApiKey;

    private final RestClient restClient;
    private final AccommodationRepository accommodationRepository;
    private final AccommodationImageRepository accommodationImageRepository;

    // 필요 시 조정 (한 숙소당 이미지가 많지 않지만, 안전하게 값 유지)
    private static final int BATCH = 500;

    @Transactional
    public int fetchAndSaveImagesForAllAccommodations() throws Exception {
        List<Accommodation> accommodations = accommodationRepository.findAll();
        int totalSaved = 0;
        for (Accommodation acc : accommodations) {
            totalSaved += fetchAndSaveImagesFor(acc);
        }
        return totalSaved;
    }

    @Transactional
    public int fetchAndSaveImagesByContentId(String contentId) throws Exception {
        log.info("[IMG] fetch contentId={}", contentId);
        Optional<Accommodation> opt = accommodationRepository.findByTourApiId(contentId);
        log.info("[IMG] accommodation exists? {}", opt.isPresent());
        if (opt.isEmpty()) return 0;
        return fetchAndSaveImagesFor(opt.get());
    }

    @Transactional
    public int fetchAndSaveImagesFor(Accommodation accommodation) throws Exception {
        String contentId = accommodation.getTourApiId();
        log.info("[IMG] fetchFor accId={}, contentId={}", accommodation.getId(), contentId);
        if (contentId == null || contentId.isBlank()) return 0;

        // ===== 0) 이 숙소의 기존 이미지들 한 번에 조회 → URL 기준 Map 구성 =====
        List<AccommodationImage> existingList = accommodationImageRepository.findByAccommodation(accommodation);
        Map<String, AccommodationImage> existingByUrl = new HashMap<>();
        for (AccommodationImage ai : existingList) {
            if (ai.getImageUrl() != null) {
                existingByUrl.put(normalize(ai.getImageUrl()), ai);
            }
        }

        // ===== 1) 대표 이미지(detailCommon2) =====
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        String commonUrl = UriComponentsBuilder.newInstance()
                .uri(URI.create("https://apis.data.go.kr/B551011/KorService2/detailCommon2"))
                .queryParam("serviceKey", tourApiKey)
                .queryParam("MobileApp", "AppTest")
                .queryParam("MobileOS", "ETC")
                .queryParam("contentId", contentId)
                .queryParam("defaultYN", "Y")
                .queryParam("firstImageYN", "Y")
                .build(false)
                .toUriString();

        String commonXml = restClient.get().uri(commonUrl).retrieve().body(String.class);
        log.info("[IMG] commonXml length={}", commonXml == null ? 0 : commonXml.length());

        String firstimage = null;
        String firstimage2 = null;
        if (commonXml != null && !commonXml.isBlank()) {
            try {
                Document doc = builder.parse(new InputSource(new StringReader(commonXml)));
                NodeList items = doc.getElementsByTagName("item");
                if (items.getLength() > 0) {
                    Element item = (Element) items.item(0);
                    firstimage  = getTagValueSafe("firstimage", item);
                    firstimage2 = getTagValueSafe("firstimage2", item);
                }
            } catch (Exception ex) {
                log.warn("[IMG] detailCommon2 parse error: {}", ex.getMessage());
            }
        }

        String thumbnailUrl = null;
        if (firstimage != null && !firstimage.isBlank()) {
            thumbnailUrl = firstimage.trim();
        } else if (firstimage2 != null && !firstimage2.isBlank()) {
            thumbnailUrl = firstimage2.trim();
        }
        log.info("[IMG] firstimage={}, firstimage2={}, chosenThumb={}", firstimage, firstimage2, thumbnailUrl);

        // ===== 2) 서브 이미지(detailImage2) =====
        String imageUrl = UriComponentsBuilder.newInstance()
                .uri(URI.create("https://apis.data.go.kr/B551011/KorService2/detailImage2"))
                .queryParam("serviceKey", tourApiKey)
                .queryParam("MobileApp", "AppTest")
                .queryParam("MobileOS", "ETC")
                .queryParam("contentId", contentId)
                .queryParam("imageYN", "Y")
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 100)
                .build(false)
                .toUriString();

        String listXml = restClient.get().uri(imageUrl).retrieve().body(String.class);
        log.info("[IMG] listXml length={}", listXml == null ? 0 : listXml.length());

        // 중복 제거를 위해 Set 사용
        List<String> subUrls = new ArrayList<>();
        if (listXml != null && !listXml.isBlank()) {
            try {
                Document doc = builder.parse(new InputSource(new StringReader(listXml)));
                String resultCode = getTagValueSafe("resultCode", doc);
                String resultMsg  = getTagValueSafe("resultMsg",  doc);
                if (resultCode != null) {
                    log.info("[IMG] detailImage resultCode={}, msg={}", resultCode, resultMsg);
                }

                NodeList items = doc.getElementsByTagName("item");
                if (items.getLength() == 0) {
                    log.info("[IMG] no sub images (detailImage2 items=0) for contentId={}", contentId);
                }

                for (int i = 0; i < items.getLength(); i++) {
                    Element item = (Element) items.item(i);
                    String origin = getTagValueSafe("originimgurl", item);
                    String small  = getTagValueSafe("smallimageurl", item);
                    if (origin != null && !origin.isBlank()) subUrls.add(origin.trim());
                    if (small  != null && !small.isBlank())  subUrls.add(small.trim());
                }
            } catch (Exception e) {
                log.warn("[IMG] detailImage2 parse error: {}", e.getMessage());
            }
        }

        // 대표 이미지 없으면 첫 번째 서브 이미지를 썸네일로 승격
        if (thumbnailUrl == null && !subUrls.isEmpty()) {
            thumbnailUrl = subUrls.get(0);
            log.info("[IMG] promoted first sub image as thumbnail: {}", thumbnailUrl);
        }

        // ===== 3) 삽입/업데이트 목록 구성 → saveAll =====
        List<AccommodationImage> toInsert = new ArrayList<>();
        List<AccommodationImage> toUpdate = new ArrayList<>();

        // 대표 이미지 먼저 처리(있다면)
        if (thumbnailUrl != null) {
            upsertImage(accommodation, thumbnailUrl, true, existingByUrl, toInsert, toUpdate);
        }

        // 서브 이미지 처리 (대표 이미지와 중복될 수 있어도 upsertImage에서 정리)
        for (String url : subUrls) {
            upsertImage(accommodation, url, false, existingByUrl, toInsert, toUpdate);
        }

        // 청크 저장 (현재는 보통 100장 이하지만 공통 패턴 유지)
        int savedNew = 0;
        for (int i = 0; i < toInsert.size(); i += BATCH) {
            int end = Math.min(i + BATCH, toInsert.size());
            accommodationImageRepository.saveAll(toInsert.subList(i, end));
            savedNew += (end - i); // 신규만 카운트(이전 코드 정책 유지)
        }
        for (int i = 0; i < toUpdate.size(); i += BATCH) {
            int end = Math.min(i + BATCH, toUpdate.size());
            accommodationImageRepository.saveAll(toUpdate.subList(i, end));
        }

        log.info("[IMG] saved new={}, updatedThumb={}", savedNew, toUpdate.size());
        return savedNew; // 컨트롤러 메시지 정책에 맞춰 '신규'만 카운트(원래 로직과 동일)
    }

    @Transactional(readOnly = true)
    public List<AccommodationImage> getImages(Long accommodationId) {
        Optional<Accommodation> opt = accommodationRepository.findById(accommodationId);
        return opt.map(accommodationImageRepository::findByAccommodation).orElseGet(List::of);
    }

    // ========= 헬퍼 =========

    private void upsertImage(
            Accommodation acc,
            String rawUrl,
            boolean preferThumbnail,
            Map<String, AccommodationImage> existingByUrl,
            List<AccommodationImage> toInsert,
            List<AccommodationImage> toUpdate
    ) {
        if (rawUrl == null) return;
        String url = normalize(rawUrl);
        if (url.isEmpty()) return;

        AccommodationImage exist = existingByUrl.get(url);
        if (exist != null) {
            if (preferThumbnail && !exist.isThumbnail()) {
                exist.setThumbnail(true);  // 썸네일 승격만 업데이트
                toUpdate.add(exist);
            }
            return; // 신규 아님
        }

        // 신규
        AccommodationImage img = AccommodationImage.builder()
                .accommodation(acc)
                .imageUrl(url)
                .thumbnail(preferThumbnail)
                .build();
        toInsert.add(img);
        // Map에도 추가해두면 이후 중복 URL이 들어와도 신규로 또 담지 않음
        existingByUrl.put(url, img);
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", "");
    }

    private String getTagValueSafe(String tag, Document doc) {
        if (doc == null) return null;
        return getTagValueSafe(tag, doc.getDocumentElement());
    }

    private String getTagValueSafe(String tag, Element element) {
        if (element == null) return null;
        NodeList nodes = element.getElementsByTagName(tag);
        if (nodes.getLength() == 0) return null;
        String text = nodes.item(0).getTextContent();
        return (text == null) ? null : text.trim();
    }
}
