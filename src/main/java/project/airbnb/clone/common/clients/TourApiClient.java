package project.airbnb.clone.common.clients;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(headers = "Accept: application/json")
public interface TourApiClient {

    @GetExchange("/areaBasedSyncList2?serviceKey={serviceKey}&MobileApp={MobileApp}&MobileOS={MobileOS}&_type={type}&contentTypeId={contentTypeId}")
    JsonNode getAreaList(@RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
                         @RequestParam(value = "numOfRows", defaultValue = "30") int numOfRows);

    @GetExchange("/detailCommon2?serviceKey={serviceKey}&MobileApp={MobileApp}&MobileOS={MobileOS}&_type={type}")
    JsonNode detailCommon(@RequestParam("contentId") String contentId,
                          @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
                          @RequestParam(value = "numOfRows", defaultValue = "30") int numOfRows);

    @GetExchange("/detailIntro2?serviceKey={serviceKey}&MobileApp={MobileApp}&MobileOS={MobileOS}&_type={type}&contentTypeId={contentTypeId}")
    JsonNode detailIntro(@RequestParam(value = "contentId") String contentId,
                         @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
                         @RequestParam(value = "numOfRows", defaultValue = "30") int numOfRows);

    @GetExchange("/detailInfo2?serviceKey={serviceKey}&MobileApp={MobileApp}&MobileOS={MobileOS}&_type={type}&contentTypeId={contentTypeId}")
    JsonNode detailInfo(@RequestParam(value = "contentId") String contentId,
                        @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
                        @RequestParam(value = "numOfRows", defaultValue = "30") int numOfRows);

    @GetExchange("/detailImage2?serviceKey={serviceKey}&MobileApp={MobileApp}&MobileOS={MobileOS}&_type={type}")
    JsonNode detailImage(@RequestParam(value = "contentId") String contentId,
                         @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
                         @RequestParam(value = "numOfRows", defaultValue = "30") int numOfRows,
                         @RequestParam(value = "imageYN", defaultValue = "Y") String imageYN);

    @GetExchange("/areaCode2?serviceKey={serviceKey}&MobileApp={MobileApp}&MobileOS={MobileOS}&_type={type}")
    JsonNode areaCode(@RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
                      @RequestParam(value = "numOfRows", defaultValue = "30") int numOfRows,
                      @RequestParam(value = "areaCode") String areaCode);
}
