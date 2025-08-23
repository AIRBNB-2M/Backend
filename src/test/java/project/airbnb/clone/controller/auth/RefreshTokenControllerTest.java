package project.airbnb.clone.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.service.jwt.TokenService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RefreshTokenController.class)
class RefreshTokenControllerTest extends RestDocsTestSupport {

    @MockitoBean
    TokenService tokenService;

    @Test
    @DisplayName("쿠키로 받은 리프레시 토큰으로 액세스 토큰을 갱신한다.")
    void refreshAccessToken() throws Exception {
        //given
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);

            response.addHeader(AUTHORIZATION, "Bearer {dummy-access-token}");
            response.addHeader(SET_COOKIE, "RefreshToken={dummy-refresh-token}; Path=/; HttpOnly");
            return null;
        }).when(tokenService).refreshAccessToken(any(), any(), any());

        //when
        //then
        mockMvc.perform(post("/api/auth/refresh")
                       .cookie(new Cookie("RefreshToken", "refresh-token"))
               )
               .andExpect(status().isOk())
               .andDo(restDocs.document(
                       requestCookies(
                               cookieWithName("RefreshToken").description("로그인 시 전달된 리프레시 토큰")
                       ),
                       responseHeaders(
                               headerWithName(AUTHORIZATION).description("새로운 액세스 토큰 발급"),
                               headerWithName(SET_COOKIE).description("새로운 리프레시 토큰 발급")
                       )
               ));
    }

    @Test
    @DisplayName("액세스 토큰과 리프레시 토큰을 받아 로그아웃 처리를 한다.")
    void logout() throws Exception {
        //given

        //when
        //then
        mockMvc.perform(post("/api/auth/logout")
                       .cookie(new Cookie("RefreshToken", "{refresh-token}"))
                       .header(AUTHORIZATION,"Bearer {access-token}")
               )
               .andExpect(status().isOk())
               .andDo(restDocs.document(
                       requestCookies(
                               cookieWithName("RefreshToken").description("로그인 시 전달된 리프레시 토큰")
                       ),
                       requestHeaders(
                               headerWithName(AUTHORIZATION).description("로그인 시 전달된 액세스 토큰")
                       ),
                       responseHeaders(
                               headerWithName(SET_COOKIE).description("무효 처리된 리프레시 토큰")
                       )
               ));
    }
}