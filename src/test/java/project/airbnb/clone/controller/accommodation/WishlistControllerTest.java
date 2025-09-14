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
import project.airbnb.clone.dto.wishlist.WishlistCreateReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateResDto;
import project.airbnb.clone.dto.wishlist.WishlistUpdateReqDto;
import project.airbnb.clone.service.accommodation.WishlistService;
import project.airbnb.clone.service.jwt.TokenService;

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
}