package project.airbnb.clone.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.WithMockMember;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.dto.member.SignupRequestDto;
import project.airbnb.clone.service.member.EmailVerificationService;
import project.airbnb.clone.service.member.MemberService;

import java.time.LocalDate;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends RestDocsTestSupport {

    public static final String AUTH_API_TAG = "Auth API";

    @MockitoBean MemberService memberService;
    @MockitoBean EmailVerificationService emailVerificationService;

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
               .andExpectAll(
                       handler().handlerType(AuthController.class),
                       handler().methodName("refreshAccessToken"),
                       status().isOk()
               )
               .andDo(document("refresh-accessToken",
                       requestCookies(cookieWithName("RefreshToken").description("로그인 시 전달된 리프레시 토큰")),
                       resource(
                               builder()
                                       .tag(AUTH_API_TAG)
                                       .summary("액세스 토큰 갱신")
                                       .description("Key=RefreshToken 쿠키로 RefreshToken을 전달해주세요.")
                                       .responseHeaders(
                                               headerWithName(AUTHORIZATION).description("새로운 액세스 토큰 발급"),
                                               headerWithName(SET_COOKIE).description("새로운 리프레시 토큰 발급")
                                       )
                                       .build()

                       )));
    }

    @Test
    @DisplayName("액세스 토큰과 리프레시 토큰을 받아 로그아웃 처리를 한다.")
    void logout() throws Exception {
        //given

        //when
        //then
        mockMvc.perform(post("/api/auth/logout")
                       .cookie(new Cookie("RefreshToken", "{refresh-token}"))
                       .header(AUTHORIZATION, "Bearer {access-token}")
               )
               .andExpectAll(
                       handler().handlerType(AuthController.class),
                       handler().methodName("logout"),
                       status().isOk()
               )
               .andDo(document("logout",
                       requestCookies(cookieWithName("RefreshToken").description("로그인 시 전달된 리프레시 토큰")),
                       resource(
                               builder()
                                       .tag(AUTH_API_TAG)
                                       .summary("로그아웃")
                                       .description("Key=RefreshToken 쿠키로 RefreshToken을 전달해주세요.")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("로그인 시 전달된 액세스 토큰"))
                                       .responseHeaders(headerWithName(SET_COOKIE).description("무효 처리된 리프레시 토큰"))
                                       .build()
                       )));
    }

    @Test
    @DisplayName("REST 회원 가입")
    void signup() throws Exception {
        //given
        SignupRequestDto requestDto = new SignupRequestDto(
                "Chris Shu", "email@test.com", "01012345678", LocalDate.of(2000, 9, 14), "password12@"
        );

        //when
        //then
        mockMvc.perform(post("/api/auth/signup")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(requestDto))
               )
               .andExpectAll(
                       handler().handlerType(AuthController.class),
                       handler().methodName("signup"),
                       status().isCreated()
               )
               .andDo(document("rest-signup",
                       resource(
                               builder()
                                       .tag(AUTH_API_TAG)
                                       .summary("REST 회원 가입")
                                       .requestFields(
                                               fieldWithPath("name")
                                                       .type(JsonFieldType.STRING)
                                                       .description("이름"),
                                               fieldWithPath("email")
                                                       .type(JsonFieldType.STRING)
                                                       .description("이메일 (제약사항 : 이메일 형식 준수)"),
                                               fieldWithPath("number")
                                                       .type(JsonFieldType.STRING)
                                                       .description("전화번호 (제약사항 : 하이픈(-) 제외)")
                                                       .optional(),
                                               fieldWithPath("birthDate")
                                                       .type(JsonFieldType.STRING)
                                                       .description("생일 (제약사항 : 과거일)")
                                                       .optional(),
                                               fieldWithPath("password")
                                                       .type(JsonFieldType.STRING)
                                                       .description("비밀번호 (제약사항 : 8~15자리, 특수문자 포함)")
                                       )
                                       .requestSchema(schema("SignupRequest"))
                                       .build()
                       )));
    }

    @Test
    @DisplayName("이메일 인증 요청")
    @WithMockMember
    void sendEmail() throws Exception {
        //given

        //when
        //then
        mockMvc.perform(post("/api/auth/email/verify")
                       .header(AUTHORIZATION, "Bearer {access-token}")
               )
               .andExpectAll(
                       handler().handlerType(AuthController.class),
                       handler().methodName("sendEmail"),
                       status().isOk()
               )
               .andDo(document("send-email",
                       resource(
                               builder()
                                       .tag(AUTH_API_TAG)
                                       .summary("이메일 인증 요청")
                                       .description("회원가입 과정에서 저장된 이메일로 인증을 진행합니다.")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .build()
                       )
               ));
    }
}