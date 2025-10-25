package project.airbnb.clone.controller.reservation;

import com.epages.restdocs.apispec.SimpleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.WithMockGuest;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.dto.reservation.PostReviewReqDto;
import project.airbnb.clone.service.reservation.ReservationService;

import java.math.BigDecimal;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest extends RestDocsTestSupport {

    private static final String RESERVATION_API_TAG = "Reservation API";

    @MockitoBean ReservationService reservationService;

    @Test
    @DisplayName("예약 리뷰 등록")
    @WithMockGuest
    void postReview() throws Exception {
        //given
        PostReviewReqDto requestDto = new PostReviewReqDto(BigDecimal.valueOf(4.5), "만족스러운 여행이었어요!");

        //when
        //then
        mockMvc.perform(post("/api/reservations/{reservationId}/reviews", 1L)
                       .header(AUTHORIZATION, "Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(requestDto)))
               .andExpectAll(
                       handler().handlerType(ReservationController.class),
                       handler().methodName("postReview"),
                       status().isCreated()
               )
               .andDo(
                       document("post-review",
                               resource(
                                       builder()
                                               .tag(RESERVATION_API_TAG)
                                               .summary("예약 리뷰 등록")
                                               .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                               .pathParameters(parameterWithName("reservationId").type(SimpleType.NUMBER).description("리뷰 등록할 예약 ID"))
                                               .requestFields(
                                                       fieldWithPath("rating")
                                                               .description("별점 (0.0 ~ 5.0)")
                                                               .type(NUMBER),
                                                       fieldWithPath("content")
                                                               .description("내용 (최대 100자)")
                                                               .type(STRING)
                                               )
                                               .requestSchema(schema("PostReviewRequest"))
                                               .build()
                               )
                       ));

    }
}