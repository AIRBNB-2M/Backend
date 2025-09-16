package project.airbnb.clone.controller.accommodation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.accommodation.DetailAccommodationResDto;
import project.airbnb.clone.dto.accommodation.DetailAccommodationResDto.DetailImageDto;
import project.airbnb.clone.dto.accommodation.DetailAccommodationResDto.DetailReviewDto;
import project.airbnb.clone.dto.accommodation.FilteredAccListResDto;
import project.airbnb.clone.dto.accommodation.MainAccListResDto;
import project.airbnb.clone.dto.accommodation.MainAccResDto;
import project.airbnb.clone.service.accommodation.AccommodationService;
import project.airbnb.clone.service.jwt.TokenService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static project.airbnb.clone.config.RestDocsConfig.field;

@WebMvcTest(AccommodationController.class)
class AccommodationControllerTest extends RestDocsTestSupport {

    @MockitoBean
    AccommodationService accommodationService;
    @MockitoBean
    TokenService tokenService;

    @Test
    @DisplayName("메인 페이지 숙소 목록 조회")
    void getAccommodations() throws Exception {
        // given
        List<MainAccListResDto> seoulAcc = List.of(
                new MainAccListResDto(1L, "호텔A", 100000, 4.5, "https://example.com/a.jpg", true, 1L),
                new MainAccListResDto(2L, "호텔B", 200000, 3.8, "https://example.com/b.jpg", false, null)
        );
        List<MainAccListResDto> gyeonggiAcc = List.of(
                new MainAccListResDto(3L, "호텔C", 150000, 4.3, "https://example.com/c.jpg", false, null),
                new MainAccListResDto(4L, "호텔D", 250000, 4.7, "https://example.com/d.jpg", true, 2L),
                new MainAccListResDto(5L, "호텔E", 300000, 3.3, "https://example.com/e.jpg", true, 2L)
        );

        List<MainAccResDto> result = List.of(
                new MainAccResDto("서울", "code-1", seoulAcc),
                new MainAccResDto("경기도", "code-2", gyeonggiAcc)
        );

        given(accommodationService.getAccommodations(any())).willReturn(result);

        //when
        //then
        mockMvc.perform(get("/api/accommodations"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(result.size())))
               .andExpect(jsonPath("$[0].areaName").value(result.get(0).areaName()))
               .andExpect(jsonPath("$[1].areaName").value(result.get(1).areaName()))
               .andExpect(jsonPath("$[0].accommodations", hasSize(result.get(0).accommodations().size())))
               .andExpect(jsonPath("$[1].accommodations", hasSize(result.get(1).accommodations().size())))
               .andExpect(jsonPath("$[0].accommodations[0].reservationCount").doesNotExist())
               .andExpect(jsonPath("$[0].accommodations[0].areaCode").doesNotExist())
               .andDo(restDocs.document(
                       requestHeaders(
                               headerWithName(AUTHORIZATION).optional().description("Bearer {액세스 토큰}")
                       ),
                       responseFields(
                               fieldWithPath("[].areaName")
                                       .attributes(field("path", "areaName"))
                                       .description("지역명"),
                               fieldWithPath("[].areaCode")
                                       .attributes(field("path", "areaCode"))
                                       .description("지역 코드"),
                               fieldWithPath("[].accommodations[].accommodationId")
                                       .attributes(field("path", "accommodationId"))
                                       .description("숙소 ID"),
                               fieldWithPath("[].accommodations[].title")
                                       .attributes(field("path", "title"))
                                       .description("숙소 이름"),
                               fieldWithPath("[].accommodations[].price")
                                       .attributes(field("path", "price"))
                                       .description("숙소 가격"),
                               fieldWithPath("[].accommodations[].avgRate")
                                       .attributes(field("path", "avgRate"))
                                       .description("평균 평점"),
                               fieldWithPath("[].accommodations[].thumbnailUrl")
                                       .attributes(field("path", "thumbnailUrl"))
                                       .description("썸네일 URL"),
                               fieldWithPath("[].accommodations[].isInWishlist")
                                       .attributes(field("path", "isInWishlist"))
                                       .description("위시리스트에 저장된 숙소인지 여부"),
                               fieldWithPath("[].accommodations[].wishlistId")
                                       .attributes(field("path", "wishlistId"))
                                       .optional()
                                       .description("저장된 위시리스트 ID (isInWishlist = true일 때만, false면 null)")
                       )
               ));
    }

    @Test
    @DisplayName("숙소 검색 조회 (페이징)")
    void getFilteredPagingAccommodations() throws Exception {
        //given
        List<FilteredAccListResDto> dtos = List.of(
                new FilteredAccListResDto(1L, "title-1", 50000, 4.3, 10,
                        List.of("https://example.com/a.jpg", "https://example.com/b.jpg"), false, null),
                new FilteredAccListResDto(2L, "title-2", 80000, 4.5, 23,
                        List.of("https://example.com/c.jpg", "https://example.com/d.jpg"), true, 1L)
        );

        PageResponseDto<FilteredAccListResDto> response = PageResponseDto.<FilteredAccListResDto>builder()
                                                                         .contents(dtos)
                                                                         .pageNumber(0)
                                                                         .pageSize(15)
                                                                         .total(dtos.size())
                                                                         .build();

        given(accommodationService.getFilteredPagingAccommodations(any(), any(), any()))
                .willReturn(response);

        //when
        //then
        mockMvc.perform(get(("/api/accommodations/search"))
                       .param("areaCode", "32")
                       .param("amenities", "roomtv")
                       .param("amenities", "sports")
                       .param("priceGoe", "100000")
                       .param("priceLoe", "300000")
                       .param("page", "0")
                       .param("size", "15"))
               .andExpectAll(
                       status().isOk(),
                       jsonPath("$.contents.length()").value(dtos.size())
               )
               .andDo(restDocs.document(
                       queryParameters(
                               parameterWithName("areaCode").optional().description("지역코드"),
                               parameterWithName("amenities").optional().description("편의시설(다중 선택)"),
                               parameterWithName("priceGoe").optional().description("숙소 최소 가격"),
                               parameterWithName("priceLoe").optional().description("숙소 최대 가격"),
                               parameterWithName("page").optional().description("페이지 크기"),
                               parameterWithName("size").optional().description("페이지 번호 (0-index)")
                       ),
                       requestHeaders(headerWithName(AUTHORIZATION).optional().description("Bearer {액세스 토큰}")),
                       responseFields(
                               fieldWithPath("contents")
                                       .attributes(field("path", "contents"))
                                       .description("검색 페이지 데이터"),
                               fieldWithPath("hasPrev")
                                       .attributes(field("path", "hasPrev"))
                                       .description("이전 페이지 존재 여부"),
                               fieldWithPath("hasNext")
                                       .attributes(field("path", "hasNext"))
                                       .description("다음 페이지 존재 여부"),
                               fieldWithPath("totalCount")
                                       .attributes(field("path", "totalCount"))
                                       .description("검색된 전체 데이터 개수"),
                               fieldWithPath("prevPage")
                                       .attributes(field("path", "prevPage"))
                                       .description("이전 페이지 번호 (0-index, 없으면 -1)"),
                               fieldWithPath("nextPage")
                                       .attributes(field("path", "nextPage"))
                                       .description("다음 페이지 번호 (0-index, 없으면 -1)"),
                               fieldWithPath("totalPage")
                                       .attributes(field("path", "totalPage"))
                                       .description("총 페이지 개수"),
                               fieldWithPath("current")
                                       .attributes(field("path", "current"))
                                       .description("현재 페이지 번호 (0-index)"),
                               fieldWithPath("size")
                                       .attributes(field("path", "size"))
                                       .description("페이지 크기"),
                               fieldWithPath("contents[].accommodationId")
                                       .attributes(field("path", "accommodationId"))
                                       .description("숙소 ID"),
                               fieldWithPath("contents[].title")
                                       .attributes(field("path", "title"))
                                       .description("숙소 제목"),
                               fieldWithPath("contents[].price")
                                       .attributes(field("path", "price"))
                                       .description("숙소 가격"),
                               fieldWithPath("contents[].avgRate")
                                       .attributes(field("path", "avgRate"))
                                       .description("평균 평점"),
                               fieldWithPath("contents[].avgCount")
                                       .attributes(field("path", "avgCount"))
                                       .description("리뷰 개수"),
                               fieldWithPath("contents[].imageUrls")
                                       .attributes(field("path", "imageUrls"))
                                       .description("숙소의 이미지 목록(최대 10장)"),
                               fieldWithPath("contents[].isInWishlist")
                                       .attributes(field("path", "isInWishlist"))
                                       .description("위시리스트에 저장된 숙소인지 여부"),
                               fieldWithPath("contents[].wishlistId")
                                       .attributes(field("path", "wishlistId"))
                                       .optional()
                                       .description("저장된 위시리스트 ID (isInWishlist = true일 때만, false면 null)")
                       )
               ));
    }

    @Test
    @DisplayName("특정 숙소 상세 조회")
    void getAccommodation() throws Exception {
        //given
        Long accommodationId = 1L;
        DetailImageDto detailImageDto = new DetailImageDto(
                "https://example.com/thumbnail.jpg",
                List.of("https://example.com/a.jpg", "https://example.com/b.jpg")
        );
        List<String> amenities = List.of("바비큐장", "뷰티시설", "식음료장", "자전거대여");

        LocalDateTime now = LocalDateTime.now();
        List<DetailReviewDto> reviewDtos = List.of(
                new DetailReviewDto(1L, "guest-A", "https://example.com/profile-A.jpg", now, now, 4.5, "review-content-1"),
                new DetailReviewDto(2L, "guest-B", "https://example.com/profile-B.jpg", now, now, 4.7, "review-content-2")
        );
        DetailAccommodationResDto response = new DetailAccommodationResDto(accommodationId, "acc-title", 5, "경기도 부천시...", 35.3, 40.1,
                "10:00", "14:00", "acc-overview", "054-855-8552", "7일 이내 100%",
                55000, true, 1L, 4.8, detailImageDto, amenities, reviewDtos
        );

        given(accommodationService.getDetailAccommodation(any(), any())).willReturn(response);

        //when
        //then
        mockMvc.perform(get("/api/accommodations/{id}", accommodationId))
               .andExpectAll(
                       status().isOk(),
                       jsonPath("$.accommodationId").value(response.accommodationId()),
                       jsonPath("$.title").value(response.title()),
                       jsonPath("$.maxPeople").value(response.maxPeople()),
                       jsonPath("$.address").value(response.address()),
                       jsonPath("$.mapX").value(response.mapX()),
                       jsonPath("$.mapY").value(response.mapY()),
                       jsonPath("$.checkIn").value(response.checkIn()),
                       jsonPath("$.checkOut").value(response.checkOut()),
                       jsonPath("$.description").value(response.description()),
                       jsonPath("$.number").value(response.number()),
                       jsonPath("$.refundRegulation").value(response.refundRegulation()),
                       jsonPath("$.price").value(response.price()),
                       jsonPath("$.isInWishlist").value(response.isInWishlist()),
                       jsonPath("$.wishlistId").value(response.wishlistId()),
                       jsonPath("$.avgRate").value(response.avgRate()),
                       jsonPath("$.images.thumbnail").value(detailImageDto.thumbnail()),
                       jsonPath("$.images.others.length()").value(detailImageDto.others().size()),
                       jsonPath("$.amenities.length()").value(amenities.size()),
                       jsonPath("$.reviews.length()").value(reviewDtos.size())
               )
               .andDo(restDocs.document(
                       pathParameters(
                               parameterWithName("id").description("검색 숙소 ID")
                       ),
                       requestHeaders(
                               headerWithName(AUTHORIZATION).optional().description("Bearer {액세스 토큰}")
                       ),
                       responseFields(
                               fieldWithPath("accommodationId")
                                       .attributes(field("path", "accommodationId"))
                                       .description("숙소 ID"),
                               fieldWithPath("title")
                                       .attributes(field("path", "title"))
                                       .description("숙소 제목"),
                               fieldWithPath("maxPeople")
                                       .optional()
                                       .attributes(field("path", "maxPeople"))
                                       .description("숙소 최대 수용 인원"),
                               fieldWithPath("address")
                                       .attributes(field("path", "address"))
                                       .description("숙소 주소"),
                               fieldWithPath("mapX")
                                       .attributes(field("path", "mapX"))
                                       .description("숙소 X 좌표 값"),
                               fieldWithPath("mapY")
                                       .attributes(field("path", "mapY"))
                                       .description("숙소 Y 좌표 값"),
                               fieldWithPath("checkIn")
                                       .optional()
                                       .attributes(field("path", "checkIn"))
                                       .description("숙소 체크인 시간"),
                               fieldWithPath("checkOut")
                                       .optional()
                                       .attributes(field("path", "checkOut"))
                                       .description("숙소 체크아웃 시간"),
                               fieldWithPath("description")
                                       .optional()
                                       .attributes(field("path", "description"))
                                       .description("숙소 상세 설명"),
                               fieldWithPath("number")
                                       .optional()
                                       .attributes(field("path", "number"))
                                       .description("숙소 번호"),
                               fieldWithPath("refundRegulation")
                                       .optional()
                                       .attributes(field("path", "refundRegulation"))
                                       .description("숙소 환불 규정"),
                               fieldWithPath("price")
                                       .attributes(field("path", "price"))
                                       .description("숙소 가격"),
                               fieldWithPath("isInWishlist")
                                       .attributes(field("path", "isInWishlist"))
                                       .description("위시리스트에 저장된 숙소인지 여부"),
                               fieldWithPath("wishlistId")
                                       .attributes(field("path", "wishlistId"))
                                       .optional()
                                       .description("저장된 위시리스트 ID (isInWishlist = true일 때만, false면 null)"),
                               fieldWithPath("avgRate")
                                       .attributes(field("path", "avgRate"))
                                       .description("평균 평점"),
                               fieldWithPath("images.thumbnail")
                                       .attributes(field("path", "thumbnail"))
                                       .description("숙소 썸네일 이미지"),
                               fieldWithPath("images.others")
                                       .attributes(field("path", "others"))
                                       .description("숙소 썸네일 외 모든 이미지 목록"),
                               fieldWithPath("amenities")
                                       .description("숙소 보유 편의시설 목록"),
                               fieldWithPath("reviews[].guestId")
                                       .attributes(field("path", "guestId"))
                                       .description("리뷰 작성자 ID"),
                               fieldWithPath("reviews[].guestName")
                                       .attributes(field("path", "guestName"))
                                       .description("리뷰 작성자명"),
                               fieldWithPath("reviews[].profileUrl")
                                       .attributes(field("path", "profileUrl"))
                                       .description("리뷰 작성자 프로필 이미지"),
                               fieldWithPath("reviews[].guestCreatedDate")
                                       .attributes(field("path", "guestCreatedDate"))
                                       .description("리뷰 작성자 가입일"),
                               fieldWithPath("reviews[].reviewCreatedDate")
                                       .attributes(field("path", "reviewCreatedDate"))
                                       .description("리뷰 작성일"),
                               fieldWithPath("reviews[].rating")
                                       .attributes(field("path", "rating"))
                                       .description("리뷰 평점"),
                               fieldWithPath("reviews[].content")
                                       .attributes(field("path", "content"))
                                       .description("리뷰 내용")
                       )
               ));
    }
}