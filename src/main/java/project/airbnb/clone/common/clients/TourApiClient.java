package project.airbnb.clone.common.clients;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(headers = "Accept: application/json")
public interface TourApiClient {

    @GetExchange("/areaBasedSyncList2?serviceKey={serviceKey}&MobileApp={MobileApp}&MobileOS={MobileOS}&_type={type}&contentTypeId={contentTypeId}")
    JsonNode getAreaList(@RequestParam("pageNo") int pageNo,
                         @RequestParam("numOfRows") int numOfRows);

    @GetExchange("/detailCommon2?serviceKey={serviceKey}&MobileApp={MobileApp}&MobileOS={MobileOS}&_type={type}")
    JsonNode detailCommon(@RequestParam("contentId") String contentId);

    @GetExchange("/detailIntro2?serviceKey={serviceKey}&MobileApp={MobileApp}&MobileOS={MobileOS}&_type={type}&contentTypeId={contentTypeId}")
    JsonNode detailIntro(@RequestParam("contentId") String contentId);

    @GetExchange("/detailInfo2?serviceKey={serviceKey}&MobileApp={MobileApp}&MobileOS={MobileOS}&_type={type}&contentTypeId={contentTypeId}")
    JsonNode detailInfo(@RequestParam("contentId") String contentId);

    @GetExchange("/detailImage2?serviceKey={serviceKey}&MobileApp={MobileApp}&MobileOS={MobileOS}&_type={type}")
    JsonNode detailImage(@RequestParam("contentId") String contentId);

    @GetExchange("/areaCode2?serviceKey={serviceKey}&MobileApp={MobileApp}&MobileOS={MobileOS}&_type={type}")
    JsonNode areaCode(@RequestParam("pageNo") int pageNo,
                      @RequestParam("numOfRows") int numOfRows,
                      @RequestParam("areaCode") String areaCode);
}
