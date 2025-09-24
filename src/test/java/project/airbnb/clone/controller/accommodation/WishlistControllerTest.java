package project.airbnb.clone.controller.accommodation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.WithMockGuest;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.dto.wishlist.AddAccToWishlistReqDto;
import project.airbnb.clone.dto.wishlist.MemoUpdateReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateResDto;
import project.airbnb.clone.dto.wishlist.WishlistDetailResDto;
import project.airbnb.clone.dto.wishlist.WishlistUpdateReqDto;
import project.airbnb.clone.dto.wishlist.WishlistsResDto;
import project.airbnb.clone.service.accommodation.WishlistService;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistController.class)
class WishlistControllerTest extends RestDocsTestSupport {

    private static final String WISHLIST_API_TAG = "Wishlist API";

    @MockitoBean WishlistService wishlistService;

    @Test
    @DisplayName("숙소 위시리스트 등록")
    @WithMockGuest
    void createWishlist() throws Exception {
        //given
        WishlistCreateReqDto requestDto = new WishlistCreateReqDto("my-wishlist");
        WishlistCreateResDto responseDto = new WishlistCreateResDto(1L, requestDto.wishlistName());

        given(wishlistService.createWishlist(any(WishlistCreateReqDto.class), anyLong())).willReturn(responseDto);

        //when
        //then
        mockMvc.perform(post("/api/wishlists")
                       .header(AUTHORIZATION, "Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(requestDto))
               )
               .andExpectAll(
                       handler().handlerType(WishlistController.class),
                       handler().methodName("createWishlist"),
                       status().isCreated(),
                       jsonPath("$.wishlistId").value(responseDto.wishlistId()),
                       jsonPath("$.wishlistName").value(responseDto.wishlistName())
               )
               .andDo(document("create-wishlist",
                       resource(
                               builder()
                                       .tag(WISHLIST_API_TAG)
                                       .summary("새로운 위시리스트 생성")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .requestFields(fieldWithPath("wishlistName")
                                               .description("생성할 위시리스트 이름 (제약사항 : 1~50자)")
                                               .type(STRING))
                                       .responseFields(
                                               fieldWithPath("wishlistId").type(NUMBER).description("생성된 위시리스트 아이디"),
                                               fieldWithPath("wishlistName").type(STRING).description("생성된 위시리스트 이름"))
                                       .requestSchema(schema("WishlistCreateRequest"))
                                       .responseSchema(schema("WishlistCreateResponse"))
                                       .build()
                       )
               ));
    }

    @Test
    @DisplayName("위시리스트에 숙소 추가")
    @WithMockGuest
    void addAccommodation() throws Exception {
        //given
        AddAccToWishlistReqDto requestDto = new AddAccToWishlistReqDto(1L);

        //when
        //then
        mockMvc.perform(post("/api/wishlists/{wishlistId}/accommodations", 1L)
                       .header(AUTHORIZATION, "Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(requestDto))
               )
               .andExpectAll(
                       handler().handlerType(WishlistController.class),
                       handler().methodName("addAccommodation"),
                       status().isCreated()
               )
               .andDo(document("add-accommodation-to-wishlist",
                       resource(
                               builder()
                                       .tag(WISHLIST_API_TAG)
                                       .summary("위시리스트에 숙소 저장")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .pathParameters(parameterWithName("wishlistId").description("숙소를 저장할 위시리스트 ID"))
                                       .requestFields(fieldWithPath("accommodationId").type(NUMBER)
                                                                                      .description("저장할 숙소 ID"))
                                       .requestSchema(schema("AddAccommodationToWishlistRequest"))
                                       .build()
                       )
               ));
    }

    @Test
    @DisplayName("위시리스트에서 숙소 제거")
    @WithMockGuest
    void removeAccommodation() throws Exception {
        //given

        //when
        //then
        mockMvc.perform(delete("/api/wishlists/{wishlistId}/accommodations/{accommodationId}", 1L, 1L)
                       .header(AUTHORIZATION, "Bearer {access-token}")
               )
               .andExpectAll(
                       handler().handlerType(WishlistController.class),
                       handler().methodName("removeAccommodation"),
                       status().isNoContent()
               )
               .andDo(document("remove-accommodation-from-wishlist",
                       resource(
                               builder()
                                       .tag(WISHLIST_API_TAG)
                                       .summary("위시리스트에서 숙소 제거")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .pathParameters(
                                               parameterWithName("wishlistId").description("숙소를 제거할 위시리스트 ID"),
                                               parameterWithName("accommodationId").description("제거할 숙소 ID")
                                       )
                                       .build()
                       )
               ));
    }

    @Test
    @DisplayName("위시리스트의 이름 변경")
    @WithMockGuest
    void updateWishlistName() throws Exception {
        //given
        WishlistUpdateReqDto requestDto = new WishlistUpdateReqDto("new-wishlist-name");

        //when
        //then
        mockMvc.perform(put("/api/wishlists/{wishlistId}", 1L)
                       .header(AUTHORIZATION, "Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(requestDto))
               )
               .andExpectAll(
                       handler().handlerType(WishlistController.class),
                       handler().methodName("updateWishlistName"),
                       status().isNoContent()
               )
               .andDo(document("update-wishlist-name",
                       resource(
                               builder()
                                       .tag(WISHLIST_API_TAG)
                                       .summary("위시리스트 이름 변경")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .pathParameters(parameterWithName("wishlistId").description("이름을 변경할 위시리스트 ID"))
                                       .requestFields(fieldWithPath("wishlistName")
                                               .type(STRING)
                                               .description("새로 변경할 위시리스트의 이름 (제약사항 : 1~50자)")
                                       )
                                       .requestSchema(schema("WishlistNameUpdateRequest"))
                                       .build()
                       )
               ));
    }

    @Test
    @DisplayName("위시리스트 제거")
    @WithMockGuest
    void removeWishlist() throws Exception {
        //given

        //when
        //then
        mockMvc.perform(delete("/api/wishlists/{wishlistId}", 1L)
                       .header(AUTHORIZATION, "Bearer {access-token}")
               )
               .andExpectAll(
                       handler().handlerType(WishlistController.class),
                       handler().methodName("removeWishlist"),
                       status().isNoContent()
               )
               .andDo(document("remove-wishlist",
                       resource(
                               builder()
                                       .tag(WISHLIST_API_TAG)
                                       .summary("위시리스트 제거")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .pathParameters(parameterWithName("wishlistId").description("제거할 위시리스트 ID"))
                                       .build()
                       )
               ));
    }

    @Test
    @DisplayName("위시리스트 조회")
    @WithMockGuest
    void getAccommodationsFromWishlist() throws Exception {
        //given
        List<WishlistDetailResDto> response = List.of(
                new WishlistDetailResDto(1L, "wishlist-1", "호텔A", "호텔A-설명", 35.3, 42.4, 4.5,
                        List.of("https://example.com/a.jpg", "https://example.com/b.jpg"), "호텔A-메모"),
                new WishlistDetailResDto(2L, "wishlist-2", "호텔B", "호텔B-설명", 25.3, 47.8, 4.8,
                        List.of("https://example.com/c.jpg", "https://example.com/d.jpg"), "호텔B-메모")
        );

        given(wishlistService.getAccommodationsFromWishlist(anyLong(), anyLong())).willReturn(response);

        //when
        //then
        mockMvc.perform(get("/api/wishlists/{wishlistId}", 1L)
                       .header(AUTHORIZATION, "Bearer {access-token}")
               )
               .andExpectAll(
                       handler().handlerType(WishlistController.class),
                       handler().methodName("getAccommodationsFromWishlist"),
                       status().isOk(),
                       jsonPath("$.length()").value(response.size()),
                       jsonPath("$[0].accommodationId").value(response.get(0).accommodationId()),
                       jsonPath("$[1].accommodationId").value(response.get(1).accommodationId())
               )
               .andDo(document("get-wishlist",
                       resource(
                               builder()
                                       .tag(WISHLIST_API_TAG)
                                       .summary("위시리스트 조회")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .pathParameters(parameterWithName("wishlistId").description("조회할 위시리스트 ID"))
                                       .responseFields(
                                               fieldWithPath("[].accommodationId")
                                                       .type(NUMBER)
                                                       .description("숙소 ID"),
                                               fieldWithPath("[].wishlistName")
                                                       .type(STRING)
                                                       .description("위시리스트 이름"),
                                               fieldWithPath("[].title")
                                                       .type(STRING)
                                                       .description("숙소 제목"),
                                               fieldWithPath("[].description")
                                                       .type(STRING)
                                                       .description("숙소 설명"),
                                               fieldWithPath("[].mapX")
                                                       .type(NUMBER)
                                                       .description("숙소 위치 X 좌표"),
                                               fieldWithPath("[].mapY")
                                                       .type(NUMBER)
                                                       .description("숙소 위치 Y 좌표"),
                                               fieldWithPath("[].avgRate")
                                                       .type(NUMBER)
                                                       .description("숙소 평균 평점"),
                                               fieldWithPath("[].imageUrls")
                                                       .attributes(key("itemsType").value("string"))
                                                       .type(ARRAY)
                                                       .description("숙소 전체 이미지 목록"),
                                               fieldWithPath("[].memo")
                                                       .optional()
                                                       .type(STRING)
                                                       .description("작성한 메모 내용")
                                       )
                                       .responseSchema(schema("WishlistDetailResponse"))
                                       .build()
                       )
               ));
    }

    @Test
    @DisplayName("위시리스트 내에 있는 숙소에 메모 수정(저장)")
    @WithMockGuest
    void updateMemo() throws Exception {
        //given
        MemoUpdateReqDto requestDto = new MemoUpdateReqDto("new-memo");

        //when
        //then
        mockMvc.perform(put("/api/wishlists/{wishlistId}/accommodations/{accommodationId}", 1L, 1L)
                       .header(AUTHORIZATION, "Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(requestDto))
               )
               .andExpectAll(
                       handler().handlerType(WishlistController.class),
                       handler().methodName("updateMemo"),
                       status().isNoContent()
               )
               .andDo(document("update-wishlist-memo",
                       resource(
                               builder()
                                       .tag(WISHLIST_API_TAG)
                                       .summary("위시리스트 숙소 메모 수정")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .pathParameters(
                                               parameterWithName("wishlistId").description("메모를 수정(저장)할 숙소가 있는 위시리스트 ID"),
                                               parameterWithName("accommodationId").description("메모 수정(저장) 대상 숙소 ID")
                                       )
                                       .requestFields(fieldWithPath("memo")
                                               .description("수정(저장)할 메모 내용 (제약사항 : 1~250자)")
                                               .type(STRING)
                                       )
                                       .requestSchema(schema("UpdateMemoRequest"))
                                       .build()
                       )
               ));
    }

    @Test
    @DisplayName("위시리스트 목록 조회")
    @WithMockGuest
    void getAllWishlists() throws Exception {
        //given
        List<WishlistsResDto> response = List.of(
                new WishlistsResDto(1L, "my-wishlist-1", "https://example.com/a.jpg", 3),
                new WishlistsResDto(2L, "my-wishlist-2", "https://example.com/b.jpg", 5)
        );

        given(wishlistService.getAllWishlists(anyLong())).willReturn(response);

        //when
        //then
        mockMvc.perform(get("/api/wishlists")
                       .header(AUTHORIZATION, "Bearer {access-token}")
               )
               .andExpectAll(
                       handler().handlerType(WishlistController.class),
                       handler().methodName("getAllWishlists"),
                       status().isOk(),
                       jsonPath("$.length()").value(response.size()),

                       jsonPath("$[0].wishlistId").value(response.get(0).wishlistId()),
                       jsonPath("$[0].name").value(response.get(0).name()),
                       jsonPath("$[0].thumbnailUrl").value(response.get(0).thumbnailUrl()),
                       jsonPath("$[0].savedAccommodations").value(response.get(0).savedAccommodations()),

                       jsonPath("$[1].wishlistId").value(response.get(1).wishlistId()),
                       jsonPath("$[1].name").value(response.get(1).name()),
                       jsonPath("$[1].thumbnailUrl").value(response.get(1).thumbnailUrl()),
                       jsonPath("$[1].savedAccommodations").value(response.get(1).savedAccommodations())
               )
               .andDo(document("get-all-wishlists",
                       resource(
                               builder()
                                       .tag(WISHLIST_API_TAG)
                                       .summary("위시리스트 목록 조회")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .responseFields(
                                               fieldWithPath("[].wishlistId")
                                                       .type(NUMBER)
                                                       .description("위시리스트 ID"),
                                               fieldWithPath("[].name")
                                                       .type(STRING)
                                                       .description("위시리스트 이름"),
                                               fieldWithPath("[].thumbnailUrl")
                                                       .type(STRING)
                                                       .description("해당 위시리스트에 가장 최근 저장된 숙소의 썸네일"),
                                               fieldWithPath("[].savedAccommodations")
                                                       .type(NUMBER)
                                                       .description("위시리스트에 저장된 숙소의 개수")
                                       )
                                       .responseSchema(schema("WishlistsResponse"))
                                       .build()
                       )
               ));
    }
}