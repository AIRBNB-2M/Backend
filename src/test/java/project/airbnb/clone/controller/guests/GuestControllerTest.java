package project.airbnb.clone.controller.guests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.dto.guest.SignupRequestDto;
import project.airbnb.clone.service.guest.GuestService;

import java.time.LocalDate;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static project.airbnb.clone.config.RestDocsConfig.field;

class GuestControllerTest extends RestDocsTestSupport {

    @MockitoBean
    protected GuestService guestService;

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
               .andExpect(status().isCreated())
               .andDo(restDocs.document(
                       requestFields(
                               fieldWithPath("name").description("name").type(JsonFieldType.STRING),
                               fieldWithPath("email").description("email").type(JsonFieldType.STRING)
                                                     .attributes(field("constraints", "이메일 형식 준수")),
                               fieldWithPath("number").description("phone number").optional().type(JsonFieldType.STRING)
                                       .attributes(field("constraints", "전화번호 형식 준수(하이픈(-) 제외")),
                               fieldWithPath("birthDate").description("birthDate").optional().type(JsonFieldType.STRING)
                                       .attributes(field("constraints", "과거 날짜")),
                               fieldWithPath("password").description("password").type(JsonFieldType.STRING)
                                                        .attributes(field("constraints", "8~15자, 특수문자 포함"))
                       )
               ));
    }
}