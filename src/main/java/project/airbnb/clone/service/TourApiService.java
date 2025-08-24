package project.airbnb.clone.service;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import lombok.RequiredArgsConstructor;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.repository.AccommodationRepository;

@Service
@RequiredArgsConstructor
public class TourApiService {

    @Value("${tourapi.key}")
    private String tourApiKey;

    private final RestClient restClient;
    private final AccommodationRepository accommodationRepository;

    @PersistenceContext
    private EntityManager em;

    private static final int BATCH = 500; // 상황 따라 200~1000 조정

    @Transactional
    public void fetchAndSaveAccommodations() throws Exception {

        String url = UriComponentsBuilder
                .newInstance()
                .uri(URI.create("https://apis.data.go.kr/B551011/KorService2/areaBasedSyncList2"))
                .queryParam("serviceKey", tourApiKey)
                .queryParam("MobileApp", "AppTest")
                .queryParam("MobileOS", "ETC")
                .queryParam("contentTypeId", 32)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 30)
                .build(false)
                .toUriString();

        String xml = restClient.get().uri(url).retrieve().body(String.class);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        NodeList itemList = doc.getElementsByTagName("item");

        List<Accommodation> toInsert = new ArrayList<>();
        List<Accommodation> toUpdate = new ArrayList<>();

        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            String address = getTagValueSafe("addr1", item);
            Double mapX = parseDouble(getTagValueSafe("mapx", item));
            Double mapY = parseDouble(getTagValueSafe("mapy", item));
            String title = getTagValueSafe("title", item);
            String number = getTagValueSafe("tel", item);
            String tourApiId = getTagValueSafe("contentid", item);

            String overview = null;
            try {
                String detailUrl = UriComponentsBuilder
                        .newInstance()
                        .uri(URI.create("https://apis.data.go.kr/B551011/KorService2/detailCommon2"))
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("MobileApp", "AppTest")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("contentId", tourApiId)
                        .queryParam("defaultYN", "Y")
                        .queryParam("overviewYN", "Y")
                        .build(false)
                        .toUriString();

                String detailXml = restClient.get().uri(detailUrl).retrieve().body(String.class);

                Document detailDoc = builder.parse(new InputSource(new StringReader(detailXml)));
                NodeList overviewNode = detailDoc.getElementsByTagName("overview");
                if (overviewNode.getLength() > 0) {
                    overview = overviewNode.item(0).getTextContent();
                }
            } catch (Exception e) { overview = null; }

            String safeDescription = (overview == null || overview.isBlank()) ? null : overview.trim();

            String checkInTime = null, checkOutTime = null;
            Short maxPeople = null;
            Integer price = null;
            try {
                String introUrl = UriComponentsBuilder
                        .newInstance()
                        .uri(URI.create("https://apis.data.go.kr/B551011/KorService2/detailIntro2"))
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("MobileApp", "AppTest")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("contentId", tourApiId)
                        .queryParam("contentTypeId", 32)
                        .build(false)
                        .toUriString();

                String introXml = restClient.get().uri(introUrl).retrieve().body(String.class);

                Document introDoc = builder.parse(new InputSource(new StringReader(introXml)));
                NodeList introItems = introDoc.getElementsByTagName("item");
                if (introItems.getLength() > 0) {
                    Element intro = (Element) introItems.item(0);

                    checkInTime = firstNonBlank(intro, "checkintime");
                    checkOutTime = firstNonBlank(intro, "checkouttime");

                    String maxPeopleRaw = firstNonBlank(intro, "roommaxcount", "accomcountlodging");
                    maxPeople = parseShortFlexible(maxPeopleRaw);

                    String priceRaw = firstNonBlank(intro,
                            "roomoffseasonminfee1", "roomoffseasonminfee2",
                            "roompeakseasonminfee1", "roompeakseasonminfee2",
                            "roomminfee");
                    price = parseIntFlexible(priceRaw);
                }
            } catch (Exception e) { /* ignore */ }

            if (price == null) {
                price = fetchAveragePriceFromDetailInfo(tourApiKey, tourApiId, restClient, builder);
            }

            if (tourApiId == null || tourApiId.isBlank()) continue;
            if (mapX == null || mapY == null) continue;

            String safeAddress = (address == null || address.isBlank()) ? null : address.trim();
            String safeTitle   = (title   == null || title.isBlank())   ? null : title.trim();
            if (safeAddress == null || safeTitle == null) continue;

            String safeCheckIn  = (checkInTime  == null || checkInTime.isBlank())  ? null : checkInTime.trim();
            String safeCheckOut = (checkOutTime == null || checkOutTime.isBlank()) ? null : checkOutTime.trim();
            String safeNumber   = (number       == null || number.isBlank())       ? null : number.trim();

            Optional<Accommodation> existing = accommodationRepository.findByTourApiId(tourApiId);

            if (existing.isPresent()) {
                Accommodation acc = existing.get();
                acc.setAddress(safeAddress);
                acc.setMapX(mapX);
                acc.setMapY(mapY);
                acc.setDescription(safeDescription);
                acc.setMaxPeople(maxPeople);
                acc.setPrice(price);
                acc.setTitle(safeTitle);
                acc.setCheckIn(safeCheckIn);
                acc.setCheckOut(safeCheckOut);
                acc.setNumber(safeNumber);
                toUpdate.add(acc);
            } else {
                Accommodation acc = Accommodation.builder()
                        .tourApiId(tourApiId)
                        .address(safeAddress)
                        .mapX(mapX)
                        .mapY(mapY)
                        .description(safeDescription)
                        .maxPeople(maxPeople)
                        .price(price)
                        .title(safeTitle)
                        .checkIn(safeCheckIn)
                        .checkOut(safeCheckOut)
                        .number(safeNumber)
                        .build();
                toInsert.add(acc);
            }

            if (toInsert.size() >= BATCH) {
                accommodationRepository.saveAll(toInsert);
                accommodationRepository.flush();
                em.clear();
                toInsert.clear();
            }
            if (toUpdate.size() >= BATCH) {
                accommodationRepository.saveAll(toUpdate);
                accommodationRepository.flush();
                em.clear();
                toUpdate.clear();
            }
        }

        if (!toInsert.isEmpty()) {
            accommodationRepository.saveAll(toInsert);
        }
        if (!toUpdate.isEmpty()) {
            accommodationRepository.saveAll(toUpdate);
        }
        accommodationRepository.flush();
        em.clear();
    }

    private Double parseDouble(String value) {
        try {
            return value != null && !value.isEmpty() ? Double.parseDouble(value) : null;
        } catch (Exception e) { return null; }
    }

    private Integer parseIntFlexible(String value) {
        try {
            if (value == null) return null;
            String digits = value.replaceAll("[^0-9-]", "");
            return digits.isEmpty() ? null : Integer.parseInt(digits);
        } catch (Exception e) { return null; }
    }

    private Short parseShortFlexible(String value) {
        try {
            if (value == null) return null;
            String digits = value.replaceAll("[^0-9-]", "");
            return digits.isEmpty() ? null : Short.parseShort(digits);
        } catch (Exception e) { return null; }
    }

    private String getTagValueSafe(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag);
        if (nodes.getLength() == 0) return null;
        Node node = nodes.item(0);
        if (node == null) return null;
        String text = node.getTextContent();
        return (text == null) ? null : text.trim();
    }

    public List<Accommodation> getAllAccommodations() {
        return accommodationRepository.findAll();
    }

    public Optional<Accommodation> getAccommodation(Long id) {
        return accommodationRepository.findById(id);
    }

    private String firstNonBlank(Element el, String... tags) {
        for (String t : tags) {
            String v = getTagValueSafe(t, el);
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private Integer fetchAveragePriceFromDetailInfo(String tourApiKey, String tourApiId,
                                                    RestClient restClient, DocumentBuilder builder) {
        try {
            String infoUrl = UriComponentsBuilder.newInstance()
                    .uri(URI.create("https://apis.data.go.kr/B551011/KorService2/detailInfo2"))
                    .queryParam("serviceKey", tourApiKey)
                    .queryParam("MobileApp", "AppTest")
                    .queryParam("MobileOS", "ETC")
                    .queryParam("contentId", tourApiId)
                    .queryParam("contentTypeId", 32)
                    .build(false).toUriString();

            String infoXml = restClient.get().uri(infoUrl).retrieve().body(String.class);
            if (infoXml == null || infoXml.isBlank()) return null;

            Document infoDoc = builder.parse(new InputSource(new StringReader(infoXml)));
            NodeList rooms = infoDoc.getElementsByTagName("item");

            long sum = 0L;
            int count = 0;
            String[] feeTags = {
                    "roomminfee","roomoffseasonminfee1","roomoffseasonminfee2",
                    "roompeakseasonminfee1","roompeakseasonminfee2"
            };

            for (int r = 0; r < rooms.getLength(); r++) {
                Element room = (Element) rooms.item(r);
                for (String tag : feeTags) {
                    Integer p = parseIntFlexible(getTagValueSafe(tag, room));
                    if (p != null && p > 0) { sum += p; count++; }
                }
            }
            if (count == 0) return null;

            long avg = Math.round((double) sum / (double) count);
            if (avg > Integer.MAX_VALUE) return Integer.MAX_VALUE;
            if (avg < Integer.MIN_VALUE) return Integer.MIN_VALUE;
            return (int) avg;
        } catch (Exception e) {
            return null;
        }
    }
}
