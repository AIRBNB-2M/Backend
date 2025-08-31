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
import project.airbnb.clone.entity.AccommodationAmenity;
import project.airbnb.clone.entity.Amenity;
import project.airbnb.clone.repository.AccommodationAmenityRepository;
import project.airbnb.clone.repository.AccommodationRepository;
import project.airbnb.clone.repository.AmenityRepository;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourAmenityService {

    @Value("${tourapi.key}")
    private String tourApiKey;

    private final RestClient restClient;
    private final AccommodationRepository accommodationRepository;
    private final AmenityRepository amenityRepository;
    private final AccommodationAmenityRepository accommodationAmenityRepository;

    private static final int BATCH = 500; // 필요 시 조정

    @Transactional
    public int fetchAndSaveAmenitiesForAllAccommodations() throws Exception {
        List<Accommodation> accommodations = accommodationRepository.findAll();
        int totalLinked = 0;
        for (Accommodation acc : accommodations) {
            totalLinked += fetchAndSaveFor(acc);
        }
        return totalLinked;
    }

    @Transactional
    public int fetchAndSaveAmenitiesByContentId(String contentId) throws Exception {
        Optional<Accommodation> opt = accommodationRepository.findByContentId(contentId);
        if (opt.isEmpty()) return 0;
        return fetchAndSaveFor(opt.get());
    }

    @Transactional(readOnly = true)
    public List<Amenity> getAmenities(Long accommodationId) {
        return accommodationRepository.findById(accommodationId)
                .map(acc -> accommodationAmenityRepository.findByAccommodation(acc)
                        .stream().map(AccommodationAmenity::getAmenity).toList())
                .orElseGet(List::of);
    }

    @Transactional
    protected int fetchAndSaveFor(Accommodation acc) throws Exception {
        String contentId = acc.getContentId();
        if (contentId == null || contentId.isBlank()) return 0;

        Map<String, String> intro = fetchDetailIntro(contentId);
        List<String> infoTexts = fetchDetailInfoTexts(contentId);
        Map<String, String> infoFlags = fetchDetailInfoFlags(contentId);

        Set<String> names = detectAmenities(intro, infoTexts, infoFlags);
        log.info("[AMENITY] contentId={} detected={}", contentId, names);
        if (names.isEmpty()) return 0;

        List<Amenity> existing = amenityRepository.findAllByNameIn(names);
        Map<String, Amenity> byName = new HashMap<>();
        for (Amenity a : existing) byName.put(a.getName(), a);

        List<Amenity> toCreate = new ArrayList<>();
        for (String nm : names) {
            if (!byName.containsKey(nm)) {
                toCreate.add(Amenity.builder().name(nm).description(null).build());
            }
        }
        if (!toCreate.isEmpty()) {
            for (int i = 0; i < toCreate.size(); i += BATCH) {
                int end = Math.min(i + BATCH, toCreate.size());
                List<Amenity> saved = amenityRepository.saveAll(toCreate.subList(i, end));
                for (Amenity a : saved) byName.put(a.getName(), a);
            }
        }

        List<AccommodationAmenity> existingLinks = accommodationAmenityRepository.findByAccommodation(acc);
        Set<Long> linkedAmenityIds = new HashSet<>();
        for (AccommodationAmenity link : existingLinks) {
            if (link.getAmenity() != null) linkedAmenityIds.add(link.getAmenity().getId());
        }

        List<AccommodationAmenity> toLink = new ArrayList<>();
        for (String nm : names) {
            Amenity a = byName.get(nm);
            if (a == null) continue;
            if (!linkedAmenityIds.contains(a.getId())) {
                toLink.add(AccommodationAmenity.builder()
                        .accommodation(acc)
                        .amenity(a)
                        .build());
            }
        }

        int linkedCount = 0;
        if (!toLink.isEmpty()) {
            for (int i = 0; i < toLink.size(); i += BATCH) {
                int end = Math.min(i + BATCH, toLink.size());
                accommodationAmenityRepository.saveAll(toLink.subList(i, end));
                linkedCount += (end - i);
            }
        }

        return linkedCount;
    }

    private Map<String, String> fetchDetailIntro(String contentId) throws Exception {
        String url = UriComponentsBuilder.newInstance()
                .uri(URI.create("https://apis.data.go.kr/B551011/KorService2/detailIntro2"))
                .queryParam("serviceKey", tourApiKey)
                .queryParam("MobileApp", "AppTest")
                .queryParam("MobileOS", "ETC")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", 32)
                .build(false).toUriString();

        String xml = restClient.get().uri(url).retrieve().body(String.class);
        Map<String, String> map = new HashMap<>();
        if (xml == null || xml.isBlank()) return map;

        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = b.parse(new InputSource(new StringReader(xml)));
        NodeList items = doc.getElementsByTagName("item");
        if (items.getLength() == 0) return map;

        Element el = (Element) items.item(0);
        for (String tag : POSSIBLE_INTRO_TAGS) {
            String v = getText(el, tag);
            if (v != null && !v.isBlank()) map.put(tag, v.trim());
        }
        return map;
    }

    private List<String> fetchDetailInfoTexts(String contentId) throws Exception {
        String url = UriComponentsBuilder.newInstance()
                .uri(URI.create("https://apis.data.go.kr/B551011/KorService2/detailInfo2"))
                .queryParam("serviceKey", tourApiKey)
                .queryParam("MobileApp", "AppTest")
                .queryParam("MobileOS", "ETC")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", 32)
                .queryParam("numOfRows", 9999)
                .build(false).toUriString();

        String xml = restClient.get().uri(url).retrieve().body(String.class);
        List<String> texts = new ArrayList<>();
        if (xml == null || xml.isBlank()) return texts;

        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = b.parse(new InputSource(new StringReader(xml)));
        NodeList items = doc.getElementsByTagName("item");
        for (int i = 0; i < items.getLength(); i++) {
            Element it = (Element) items.item(i);
            for (String tag : POSSIBLE_INFO_TEXT_TAGS) {
                String v = getText(it, tag);
                if (v != null && !v.isBlank()) texts.add(v.trim());
            }
        }
        return texts;
    }

    private Map<String, String> fetchDetailInfoFlags(String contentId) throws Exception {
        String url = UriComponentsBuilder.newInstance()
                .uri(URI.create("https://apis.data.go.kr/B551011/KorService2/detailInfo2"))
        .queryParam("serviceKey", tourApiKey)
        .queryParam("MobileApp", "AppTest")
        .queryParam("MobileOS", "ETC")
        .queryParam("contentId", contentId)
        .queryParam("contentTypeId", 32)
        .queryParam("numOfRows", 9999)
        .build(false).toUriString();

        String xml = restClient.get().uri(url).retrieve().body(String.class);
        Map<String,String> flags = new HashMap<>();
        if (xml == null || xml.isBlank()) return flags;

        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = b.parse(new InputSource(new StringReader(xml)));
        NodeList items = doc.getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) {
            Element it = (Element) items.item(i);
            for (String tag : POSSIBLE_INFO_FLAG_TAGS) {
                String v = getText(it, tag);
                if ("Y".equalsIgnoreCase(v)) {
                    flags.put(tag, "Y");
                }
            }
        }
        return flags;
    }

    private Set<String> detectAmenities(Map<String, String> intro, List<String> infoTexts, Map<String,String> infoFlags) {
        Set<String> out = new LinkedHashSet<>();
        for (AmenityRule rule : RULES) {
            boolean hit = false;

            for (String tag : rule.introTags()) {
                String v = intro.get(tag);
                if (isYesLike(v) || containsAny(v, rule.keywords())) { hit = true; break; }
            }
            if (!hit && !infoTexts.isEmpty()) {
                for (String t : infoTexts) {
                    if (containsAny(t, rule.keywords())) { hit = true; break; }
                }
            }
            if (!hit && infoFlags != null) {
                for (String f : rule.infoFlagTags()) {
                    if ("Y".equalsIgnoreCase(infoFlags.get(f))) { hit = true; break; }
                }
            }
            if (hit) out.add(rule.amenityName());
        }
        return out;
    }

    private boolean isYesLike(String v) {
        if (v == null) return false;
        String s = v.replaceAll("\\s+", "").toLowerCase();
        return s.contains("y") || s.contains("가능") || s.contains("있음") || s.contains("제공")
                || s.contains("운영") || s.contains("무료") || s.contains("구비");
    }

    private boolean containsAny(String text, List<String> kws) {
        if (text == null) return false;
        String t = text.toLowerCase();
        for (String k : kws) {
            if (t.contains(k.toLowerCase())) return true;
        }
        return false;
    }

    private String getText(Element el, String tag) {
        NodeList nodes = el.getElementsByTagName(tag);
        if (nodes.getLength() == 0) return null;
        String s = nodes.item(0).getTextContent();
        return (s == null) ? null : s.trim();
    }

    private static final List<String> POSSIBLE_INTRO_TAGS = List.of(
        "parkinglodging","chkcooking","barbecue","sauna","seminar","sports",
        "foodplace","publicpc","beverage","convenience","pickup","beauty",
        "bicycle","karaoke","lounge","subfacility","publicbath","publicshower",
        "publictoilet","breakfast","pets","wifi","cook","pool"
    );

    private static final List<String> POSSIBLE_INFO_TEXT_TAGS = List.of(
        "roomtitle","roomintro","roomimg1","roomimg2","roomimg3","roomimg4","roomimg5",
        "subdetailoverview","subdetailalt","roometc"
    );

    private static final List<String> POSSIBLE_INFO_FLAG_TAGS = List.of(
        "roombathfacility","roombath","roomaircondition","roomtv",
        "roominternet","roomrefrigerator","roomtoiletries","roomhairdryer", "roomcook"
    );

    private static final List<AmenityRule> RULES = List.of(
        rule("와이파이", List.of("wifi","publicpc"),
            List.of("wifi","와이파이","무선인터넷","internet"),
            List.of("roominternet")),
        rule("주차장", List.of("parkinglodging"),
            List.of("주차","parking"),
            List.of()),
        rule("조식",   List.of("breakfast","foodplace","beverage","lounge"),
            List.of("조식","아침식사","breakfast","morning"),
            List.of()),
        rule("수영장", List.of("pool","sports"),
            List.of("수영장","pool","수영"),
            List.of()),
        rule("취사",   List.of("chkcooking","cook"),
            List.of("취사","취사가능","주방","cooking","kitchen"),
            List.of("roomcook")),
        rule("바비큐", List.of("barbecue"),
            List.of("바비큐","bbq","그릴"),
            List.of()),
        rule("사우나", List.of("sauna"),
            List.of("사우나","탕","사우나실"),
            List.of()),
        rule("피트니스", List.of("sports"),
            List.of("피트니스","헬스","gym","fitness"),
            List.of()),
        rule("세탁",   List.of("convenience"),
            List.of("세탁","laundry","세탁실","세탁기"),
            List.of()),
        rule("반려동물", List.of("pets"),
            List.of("반려동물","애견","pet","dog"),
            List.of()),
        rule("픽업",   List.of("pickup"),
            List.of("픽업","셔틀","pickup","shuttle"),
            List.of()),
        rule("에어컨", List.of(), List.of("에어컨","aircon","냉방"), List.of("roomaircondition")),
        rule("TV",    List.of(), List.of("tv","텔레비전","스마트tv"), List.of("roomtv")),
        rule("냉장고", List.of(), List.of("냉장고","refrigerator"), List.of("roomrefrigerator")),
        rule("세면도구", List.of(), List.of("세면도구","toiletries"), List.of("roomtoiletries")),
        rule("드라이기", List.of(), List.of("드라이기","헤어드라이어","hair dryer"), List.of("roomhairdryer")),
        rule("욕실",  List.of("publicbath","publicshower"),
            List.of("욕실","샤워","bath"),
            List.of("roombathfacility")),
        rule("욕조",  List.of("publicbath"),
            List.of("욕조","bathtub","스파"),
            List.of("roombath"))
    );

    private static AmenityRule rule(String name, List<String> intro, List<String> kws, List<String> flags) {
        return new AmenityRule(name, intro, kws, flags);
    }

    private record AmenityRule(
        String amenityName,
        List<String> introTags,
        List<String> keywords,
        List<String> infoFlagTags
    ) {}
}
