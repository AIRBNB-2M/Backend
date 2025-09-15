package project.airbnb.clone.controller.accommodation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.WithMockGuest;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.dto.wishlist.AddAccToWishlistReqDto;
import project.airbnb.clone.dto.wishlist.MemoUpdateReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateResDto;
import project.airbnb.clone.dto.wishlist.WishlistDetailResDto;
import project.airbnb.clone.dto.wishlist.WishlistUpdateReqDto;
import project.airbnb.clone.service.accommodation.WishlistService;
import project.airbnb.clone.service.jwt.TokenService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static project.airbnb.clone.config.RestDocsConfig.field;

@WebMvcTest(WishlistController.class)
class WishlistControllerTest extends RestDocsTestSupport {

    @MockitoBean
    WishlistService wishlistService;
    @MockitoBean
    TokenService tokenService;

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
                       .header(AUTHORIZATION,"Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(requestDto))
               )
               .andExpectAll(
                       status().isCreated(),
                       jsonPath("$.wishlistId").value(responseDto.wishlistId()),
                       jsonPath("$.wishlistName").value(responseDto.wishlistName())
               )
               .andDo(restDocs.document(
                       requestHeaders(
                               headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}")
                       ),
                       requestFields(
                               fieldWithPath("wishlistName")
                                       .description("생성할 위시리스트 이름")
                                       .attributes(field("constraints", "1 ~ 50자 제한"))
                                       .type(JsonFieldType.STRING)
                               )
                       ,responseFields(
                               fieldWithPath("wishlistId").description("생성된 위시리스트 아이디"),
                               fieldWithPath("wishlistName").description("생성된 위시리스트 이름")
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
                       .header(AUTHORIZATION,"Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(requestDto))
               )
               .andExpect(status().isCreated())
               .andDo(restDocs.document(
                       requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}")),
                       pathParameters(parameterWithName("wishlistId").description("숙소를 저장할 위시리스트 ID")),
                       requestFields(fieldWithPath("accommodationId").description("저장할 숙소 ID"))
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
                       .header(AUTHORIZATION,"Bearer {access-token}")
               )
               .andExpect(status().isNoContent())
               .andDo(restDocs.document(
                       requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}")),
                       pathParameters(
                               parameterWithName("wishlistId").description("숙소를 제거할 위시리스트 ID"),
                               parameterWithName("accommodationId").description("제거할 숙소 ID")
                       )
               ));
    }

    @Test
    @DisplayName("위시리스트의 이름 변경")
    @WithMockGuest
    void updateWishlistName() throws Exception {
        //given
        WishlistUpdateReqDto requestDto = new WishlistUpdateReqDto("test-new-wishlist-name");

        //when
        //then
        mockMvc.perform(put("/api/wishlists/{wishlistId}", 1L)
                       .header(AUTHORIZATION, "Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(requestDto))
               )
               .andExpect(status().isNoContent())
               .andDo(restDocs.document(
                       requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}")),
                       pathParameters(parameterWithName("wishlistId").description("이름을 변경할 위시리스트 ID")),
                       requestFields(fieldWithPath("wishlistName")
                               .attributes(field("constraints", "1 ~ 50자 제한"))
                               .description("새로 변경할 위시리스트의 이름"))
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
                       .header(AUTHORIZATION,"Bearer {access-token}")
               )
               .andExpect(status().isNoContent())
               .andDo(restDocs.document(
                       requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}")),
                       pathParameters(parameterWithName("wishlistId").description("제거할 위시리스트 ID"))
               ));
    }

    @Test
    @DisplayName("위시리스트 조회")
    @WithMockGuest
    void getAccommodationsFromWishlist() throws Exception {
        //given
        List<WishlistDetailResDto> response = List.of(
                new WishlistDetailResDto(1L, "호텔A", "호텔A-설명", 35.3, 42.4, 4.5,
                        List.of("https://example.com/a.jpg", "https://example.com/b.jpg"), "호텔A-메모"),
                new WishlistDetailResDto(2L, "호텔B", "호텔B-설명", 25.3, 47.8, 4.8,
                        List.of("https://example.com/c.jpg", "https://example.com/d.jpg"), "호텔B-메모")
        );

        given(wishlistService.getAccommodationsFromWishlist(anyLong(), anyLong())).willReturn(response);

        //when
        //then
        mockMvc.perform(get("/api/wishlists/{wishlistId}", 1L)
                       .header(AUTHORIZATION, "Bearer {access-token}")
               )
               .andExpectAll(
                       status().isOk(),
                       jsonPath("$.length()").value(response.size()),
                       jsonPath("$[0].accommodationId").value(response.get(0).accommodationId()),
                       jsonPath("$[1].accommodationId").value(response.get(1).accommodationId())
               )
               .andDo(restDocs.document(
                       requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}")),
                       pathParameters(parameterWithName("wishlistId").description("조회할 위시리스트 ID")),
                       responseFields(
                               fieldWithPath("[].accommodationId").attributes(field("path", "accommodationId")).description("숙소 ID"),
                               fieldWithPath("[].title").attributes(field("path", "title")).description("숙소 제목"),
                               fieldWithPath("[].description").attributes(field("path", "description")).description("숙소 설명"),
                               fieldWithPath("[].mapX").attributes(field("path", "mapX")).description("숙소 위치 X 좌표"),
                               fieldWithPath("[].mapY").attributes(field("path", "mapY")).description("숙소 위치 Y 좌표"),
                               fieldWithPath("[].avgRate").attributes(field("path", "avgRate")).description("숙소 평균 평점"),
                               fieldWithPath("[].imageUrls").attributes(field("path", "imageUrls")).description("숙소 전체 이미지 목록"),
                               fieldWithPath("[].memo").attributes(field("path", "memo")).optional().description("작성한 메모 내용")
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
                       .header(AUTHORIZATION,"Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(requestDto))
               )
               .andExpect(status().isNoContent())
               .andDo(restDocs.document(
                       requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}")),
                       pathParameters(
                               parameterWithName("wishlistId").description("메모를 수정(저장)할 숙소가 있는 위시리스트 ID"),
                               parameterWithName("accommodationId").description("메모 수정(저장) 대상 숙소 ID")
                       ),
                       requestFields(fieldWithPath("memo")
                               .attributes(field("constraints", "1 ~ 250자 제한"))
                               .description("수정(저장)할 메모 내용"))
               ));
    }
}