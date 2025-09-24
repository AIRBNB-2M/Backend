package project.airbnb.clone.controller.guests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.WithMockGuest;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.dto.guest.DefaultProfileResDto;
import project.airbnb.clone.service.guest.GuestService;

import java.time.LocalDate;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GuestController.class)
class GuestControllerTest extends RestDocsTestSupport {

    private static final String GUEST_API_TAG = "Guest API";

    @MockitoBean GuestService guestService;

    @Test
    @DisplayName("내 기본 정보 조회")
    @WithMockGuest
    void getDefaultProfile() throws Exception {
        //given
        DefaultProfileResDto response = new DefaultProfileResDto("Antonio Cui", "https://example.com/a.jpg", LocalDate.of(2024, 8, 15), "Accumsan luctus fringilla cubilia tempor auctor ullamcorper.");
        given(guestService.getDefaultProfile(anyLong())).willReturn(response);

        //when
        //then
        mockMvc.perform(get("/api/guests/me")
                       .header(AUTHORIZATION, "Bearer {access-token}")
               )
               .andExpectAll(
                       handler().handlerType(GuestController.class),
                       handler().methodName("getMyProfile"),
                       status().isOk(),
                       jsonPath("$.name").value(response.name()),
                       jsonPath("$.profileImageUrl").value(response.profileImageUrl()),
                       jsonPath("$.createdDate").value(response.createdDate().toString()),
                       jsonPath("$.aboutMe").value(response.aboutMe())
               )
               .andDo(document("get-my-profile",
                       resource(
                               builder()
                                       .tag(GUEST_API_TAG)
                                       .summary("내 프로필 정보 조회")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .responseFields(
                                               fieldWithPath("name")
                                                       .type(STRING)
                                                       .description("이름"),
                                               fieldWithPath("profileImageUrl")
                                                       .type(STRING)
                                                       .optional()
                                                       .description("프로필 이미지 URL"),
                                               fieldWithPath("createdDate")
                                                       .type(STRING)
                                                       .description("가입날짜"),
                                               fieldWithPath("aboutMe")
                                                       .type(STRING)
                                                       .optional()
                                                       .description("자기소개글")
                                       )
                                       .responseSchema(schema("DefaultProfileResponse"))
                                       .build()
                       )
               ));
    }
}