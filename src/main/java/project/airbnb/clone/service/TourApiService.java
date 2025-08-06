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
	            .queryParam("numOfRows", 10)
	            .build(false)
	            .toUriString();
        RestTemplate restTemplate = new RestTemplate();
        String xml = restTemplate.getForObject(url, String.class);

        // 2. XML 파싱
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        NodeList itemList = doc.getElementsByTagName("item");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            // areaBasedSyncList2 (엔드포인트)
            String address = getTagValue("addr1", item);
            Double mapX = parseDouble(getTagValue("mapx", item));
            Double mapY = parseDouble(getTagValue("mapy", item));
            String title = getTagValue("title", item);
            String number = getTagValue("tel", item);
            String tourApiId = getTagValue("contentid", item);

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
                    checkInTime = getTagValue("checkintime", intro);
                    checkOutTime = getTagValue("checkouttime", intro);
                    maxPeople = parseShort(getTagValue("roommaxcount", intro));
                    price = parseInt(getTagValue("roomoffseasonminfee1", intro));
                }
            } catch (Exception e) {}

            String safeCheckIn = (checkInTime == null || checkInTime.trim().isEmpty()) ? "정보없음" : checkInTime.trim();
            String safeCheckOut = (checkOutTime == null || checkOutTime.trim().isEmpty()) ? "정보없음" : checkOutTime.trim();
            String safeNumber = (number == null || number.trim().isEmpty()) ? "정보없음" : number.trim();
            
            Optional<Accommodation> existing = accommodationRepository.findByTourApiId(tourApiId);

            if (existing.isPresent()) {
                Accommodation acc = existing.get();
                acc.setAddress(address);
                acc.setMapX(mapX);
                acc.setMapY(mapY);
                acc.setDescription(overview != null ? overview : "");
                acc.setMaxPeople(maxPeople != null ? maxPeople : 0);
                acc.setPrice(price != null ? price : 0);
                acc.setTitle(title);
                acc.setCheckIn(safeCheckIn);
                acc.setCheckOut(safeCheckOut);
                acc.setNumber(safeNumber);
                accommodationRepository.save(acc);
            } else {
                Accommodation acc = Accommodation.builder()
                        .tourApiId(tourApiId)
                        .address(address)
                        .mapX(mapX)
                        .mapY(mapY)
                        .description(overview != null ? overview : "")
                        .maxPeople(maxPeople != null ? maxPeople : 0)
                        .price(price != null ? price : 0)
                        .title(title)
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
	
	private Short parseShort(String value) {
	    try {
	        return value != null && !value.isEmpty() ? Short.parseShort(value) : null;
	    } catch (Exception e) {
	        return null;
	    }
	}
	
	private Integer parseInt(String value) {
	    try {
	        return value != null && !value.isEmpty() ? Integer.parseInt(value) : null;
	    } catch (Exception e) {
	        return null;
	    }
	}

	
	private String getTagValue(String tag, Element element) {
        NodeList nlList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        return nValue == null ? null : nValue.getNodeValue();
    }
	
	public List<Accommodation> getAllAccommodations() {
	    return accommodationRepository.findAll();
	}

	public Optional<Accommodation> getAccommodation(Long id) {
	    return accommodationRepository.findById(id);
	}
}
