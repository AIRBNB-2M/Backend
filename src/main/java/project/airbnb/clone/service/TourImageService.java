package project.airbnb.clone.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.AccommodationImage;
import project.airbnb.clone.repository.AccommodationImageRepository;
import project.airbnb.clone.repository.AccommodationRepository;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourImageService {

    @Value("${tourapi.key}")
    private String tourApiKey;

    private final RestClient restClient;
    private final AccommodationRepository accommodationRepository;
    private final AccommodationImageRepository accommodationImageRepository;

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
        Optional<Accommodation> opt = accommodationRepository.findByContentId(contentId);
        log.info("[IMG] accommodation exists? {}", opt.isPresent());
        if (opt.isEmpty()) return 0;
        return fetchAndSaveImagesFor(opt.get());
    }

    @Transactional
    public int fetchAndSaveImagesFor(Accommodation accommodation) throws Exception {
        String contentId = accommodation.getContentId();
        log.info("[IMG] fetchFor accId={}, contentId={}", accommodation.getId(), contentId);
        if (contentId == null || contentId.isBlank()) return 0;

        List<AccommodationImage> existingList = accommodationImageRepository.findByAccommodation(accommodation);
        Map<String, AccommodationImage> existingByUrl = new HashMap<>();
        for (AccommodationImage ai : existingList) {
            if (ai.getImageUrl() != null) {
                existingByUrl.put(normalize(ai.getImageUrl()), ai);
            }
        }

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

        if (thumbnailUrl == null && !subUrls.isEmpty()) {
            thumbnailUrl = subUrls.get(0);
            log.info("[IMG] promoted first sub image as thumbnail: {}", thumbnailUrl);
        }

        List<AccommodationImage> toInsert = new ArrayList<>();
        List<AccommodationImage> toUpdate = new ArrayList<>();

        if (thumbnailUrl != null) {
            upsertImage(accommodation, thumbnailUrl, true, existingByUrl, toInsert, toUpdate);
        }

        for (String url : subUrls) {
            upsertImage(accommodation, url, false, existingByUrl, toInsert, toUpdate);
        }

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
        return savedNew;
    }

    @Transactional(readOnly = true)
    public List<AccommodationImage> getImages(Long accommodationId) {
        Optional<Accommodation> opt = accommodationRepository.findById(accommodationId);
        return opt.map(accommodationImageRepository::findByAccommodation).orElseGet(List::of);
    }

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
                exist.setThumbnail(true);
                toUpdate.add(exist);
            }
            return;
        }

        AccommodationImage img = AccommodationImage.builder()
                .accommodation(acc)
                .imageUrl(url)
                .thumbnail(preferThumbnail)
                .build();
        toInsert.add(img);
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
