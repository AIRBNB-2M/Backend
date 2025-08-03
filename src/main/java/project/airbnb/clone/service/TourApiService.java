package project.airbnb.clone.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.repository.AccommodationRepository;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class TourApiService {
	
	@Value("${tourapi.key}")
    private String tourApiKey;

	private final AccommodationRepository accommodationRepository;
	
	public void fetchAndSaveAccommodations() throws Exception {
		
		// 1. API 호출 (XML 응답 받기)
		String url = "https://apis.data.go.kr/B551011/KorService2/areaBasedSyncList2"
                + "?serviceKey=" + tourApiKey
                + "&MobileApp=AppTest&MobileOS=ETC&contentTypeId=32&pageNo=1&numOfRows=10";
        RestTemplate restTemplate = new RestTemplate();
        String xml = restTemplate.getForObject(url, String.class);

        // 2. XML 파싱
        List<Accommodation> list = new ArrayList<>();
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
            String contentId = getTagValue("contentid", item);

            // detailCommon2에서 overview(설명)
            String overview = null;
            try {
            	String detailUrl = "https://apis.data.go.kr/B551011/KorService2/detailCommon2"
                        + "?serviceKey=" + tourApiKey
                        + "&MobileApp=AppTest&MobileOS=ETC&contentId=" + contentId
                        + "&defaultYN=N&overviewYN=Y";
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
            	 String introUrl = "https://apis.data.go.kr/B551011/KorService2/detailIntro2"
                         + "?serviceKey=" + tourApiKey
                         + "&MobileApp=AppTest&MobileOS=ETC&contentId=" + contentId
                         + "&contentTypeId=32";
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

            Accommodation acc = Accommodation.builder()
                .address(address)
                .mapX(mapX)
                .mapY(mapY)
                .description(overview != null ? overview : "")
                .maxPeople(maxPeople != null ? maxPeople : 0)
                .price(price != null ? price : 0)
                .title(title)
                .checkIn(parseLocalTime(checkInTime))
                .checkOut(parseLocalTime(checkOutTime))
                .number(number)
                .build();

            list.add(acc);
        }


        // 3. 저장
        accommodationRepository.saveAll(list);
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

	private LocalTime parseLocalTime(String value) {
	    if (value == null || value.isEmpty()) return null;
	    try {
	        // "15:00"
	        if (value.matches("\\d{1,2}:\\d{2}")) {
	            return LocalTime.parse(value, DateTimeFormatter.ofPattern("H:mm"));
	        }
	        // "15시"
	        if (value.endsWith("시")) {
	            int hour = Integer.parseInt(value.replace("시", ""));
	            return LocalTime.of(hour, 0);
	        }
	        
	    } catch (Exception e) {
	        return null; // 못 읽는 포맷은 null 반환
	    }
	    return null;
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
