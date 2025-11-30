package project.airbnb.clone.controller.reservation;

import com.epages.restdocs.apispec.SimpleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.WithMockMember;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.dto.reservation.PostReservationReqDto;
import project.airbnb.clone.dto.reservation.PostReservationResDto;
import project.airbnb.clone.dto.reservation.PostReviewReqDto;
import project.airbnb.clone.service.reservation.ReservationService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest extends RestDocsTestSupport {

    private static final String RESERVATION_API_TAG = "Reservation API";

    @MockitoBean ReservationService reservationService;

    @Test
    @DisplayName("예약 등록")
    @WithMockMember
    void postReservation() throws Exception {
        //given
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(3);
        PostReservationReqDto request = new PostReservationReqDto(startDate, endDate, 3, 1, 1);

        PostReservationResDto response = new PostReservationResDto(
                1L, "https://example.com", "숙소-A", "일주일 내 50%...",
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59), 3, 1, 1
        );
        given(reservationService.postReservation(anyLong(), anyLong(), any())).willReturn(response);

        //when
        //then
        mockMvc.perform(post("/api/reservations/{accommodationId}", 1L)
                       .header(AUTHORIZATION, "Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(request)))
               .andExpectAll(
                       handler().handlerType(ReservationController.class),
                       handler().methodName("postReservation"),
                       status().isOk(),
                       jsonPath("$.reservationId").value(response.reservationId()),
                       jsonPath("$.thumbnailUrl").value(response.thumbnailUrl()),
                       jsonPath("$.title").value(response.title()),
                       jsonPath("$.refundRegulation").value(response.refundRegulation()),
                       jsonPath("$.startDate").exists(),
                       jsonPath("$.endDate").exists(),
                       jsonPath("$.adults").value(response.adults()),
                       jsonPath("$.children").value(response.children()),
                       jsonPath("$.infants").value(response.infants())
               )
               .andDo(
                       document("post-reservation",
                               resource(
                                       builder()
                                               .tag(RESERVATION_API_TAG)
                                               .summary("예약 등록")
                                               .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                               .pathParameters(parameterWithName("accommodationId").type(SimpleType.NUMBER).description("예약 등록할 숙소 ID"))
                                               .requestFields(
                                                       fieldWithPath("startDate")
                                                               .description("예약 시작일")
                                                               .type(STRING),
                                                       fieldWithPath("endDate")
                                                               .description("예약 종료일")
                                                               .type(STRING),
                                                       fieldWithPath("adults")
                                                               .description("예약 성인 수")
                                                               .type(NUMBER),
                                                       fieldWithPath("children")
                                                               .description("예약 어린이 수")
                                                               .type(NUMBER),
                                                       fieldWithPath("infants")
                                                               .description("예약 영유아 수")
                                                               .type(NUMBER)
                                               )
                                               .responseFields(
                                                       fieldWithPath("reservationId")
                                                               .description("예약 ID")
                                                               .type(NUMBER),
                                                       fieldWithPath("thumbnailUrl")
                                                               .description("숙소 썸네일")
                                                               .type(STRING),
                                                       fieldWithPath("title")
                                                               .description("숙소 제목")
                                                               .type(STRING),
                                                       fieldWithPath("refundRegulation")
                                                               .description("숙소 환불 규정")
                                                               .optional()
                                                               .type(STRING),
                                                       fieldWithPath("startDate")
                                                               .description("예약 시작일")
                                                               .type(STRING),
                                                       fieldWithPath("endDate")
                                                               .description("예약 종료일")
                                                               .type(STRING),
                                                       fieldWithPath("adults")
                                                               .description("예약 성인 수")
                                                               .type(NUMBER),
                                                       fieldWithPath("children")
                                                               .description("예약 어린이 수")
                                                               .type(NUMBER),
                                                       fieldWithPath("infants")
                                                               .description("예약 영유아 수")
                                                               .type(NUMBER)
                                               )
                                               .requestSchema(schema("PostReservationRequest"))
                                               .responseSchema(schema("PostReservationResponse"))
                                               .build()
                               )
                       ));

    }

    @Test
    @DisplayName("예약 리뷰 등록")
    @WithMockMember
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