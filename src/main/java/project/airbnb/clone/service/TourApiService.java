package project.airbnb.clone.service;

import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import lombok.RequiredArgsConstructor;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.repository.AccommodationRepository;

@Service
@RequiredArgsConstructor
public class TourApiService {
	
	@Value("${tourapi.key}")
    private String tourApiKey;
	
	private final RestTemplate restTemplate;

	private final AccommodationRepository accommodationRepository;
	
	public void fetchAndSaveAccommodations() throws Exception {
		
		// 1. API 호출 (XML 응답 받기)
		String url = UriComponentsBuilder
	            .newInstance()
	            .uri(URI.create("https://apis.data.go.kr/B551011/KorService2/areaBasedSyncList2"))
	            .queryParam("serviceKey", tourApiKey)
	            .queryParam("MobileApp", "AppTest")
	            .queryParam("MobileOS", "ETC")
	            .queryParam("contentTypeId", 32)
	            .queryParam("pageNo", 1)
	            .queryParam("numOfRows", 50)
	            .build(false)
	            .toUriString();
        String xml = restTemplate.getForObject(url, String.class);

        // 2. XML 파싱
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        NodeList itemList = doc.getElementsByTagName("item");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            // areaBasedSyncList2 (엔드포인트)
            String address = getTagValueSafe("addr1", item);
            Double mapX = parseDouble(getTagValueSafe("mapx", item));
            Double mapY = parseDouble(getTagValueSafe("mapy", item));
            String title = getTagValueSafe("title", item);
            String number = getTagValueSafe("tel", item);
            String tourApiId = getTagValueSafe("contentid", item);

            // detailCommon2에서 overview(설명)
            String overview = null;
            try {
            	String detailUrl = UriComponentsBuilder
                        .newInstance()
                        .uri(URI.create("https://apis.data.go.kr/B551011/KorService2/detailCommon2"))
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("MobileApp", "AppTest")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("contentId", tourApiId)
                        .queryParam("defaultYN", "N")
                        .queryParam("overviewYN", "Y")
                        .build(false)
                        .toUriString();
                String detailXml = restTemplate.getForObject(detailUrl, String.class);
                Document detailDoc = builder.parse(new InputSource(new StringReader(detailXml)));
                NodeList overviewNode = detailDoc.getElementsByTagName("overview");
                if (overviewNode.getLength() > 0) {
                    overview = overviewNode.item(0).getTextContent();
                }
            } catch (Exception e) { overview = null; }
            
            String safeDescription = (overview == null || overview.isBlank()) ? null : overview.trim();
            
            // detailIntro2에서 상세정보
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
                String introXml = restTemplate.getForObject(introUrl, String.class);
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
            } catch (Exception e) {}
            
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
                accommodationRepository.save(acc);
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
                accommodationRepository.save(acc);
            }
        }
	}
	
	private Double parseDouble(String value) {
	    try {
	        return value != null && !value.isEmpty() ? Double.parseDouble(value) : null;
	    } catch (Exception e) {
	        return null;
	    }
	}
	
	private Integer parseIntFlexible(String value) {
	    try {
	        if (value == null) return null;
	        String digits = value.replaceAll("[^0-9-]", "");
	        return digits.isEmpty() ? null : Integer.parseInt(digits);
	    } catch (Exception e) {
	        return null;
	    }
	}
	private Short parseShortFlexible(String value) {
	    try {
	        if (value == null) return null;
	        String digits = value.replaceAll("[^0-9-]", "");
	        return digits.isEmpty() ? null : Short.parseShort(digits);
	    } catch (Exception e) {
	        return null;
	    }
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

}
