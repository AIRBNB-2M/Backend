package project.airbnb.clone.controller.guests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.dto.guest.SignupRequestDto;
import project.airbnb.clone.service.guest.GuestService;
import project.airbnb.clone.service.jwt.TokenService;

import java.time.LocalDate;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GuestController.class)
class GuestControllerTest extends RestDocsTestSupport {

    private static final String GUEST_API_TAG = "Guest-API";

    @MockitoBean GuestService guestService;
    @MockitoBean TokenService tokenService;

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
                       handler().handlerType(GuestController.class),
                       handler().methodName("signup"),
                       status().isCreated()
               )
               .andDo(document("rest-signup",
                       resource(
                               builder()
                                       .tag(GUEST_API_TAG)
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
}