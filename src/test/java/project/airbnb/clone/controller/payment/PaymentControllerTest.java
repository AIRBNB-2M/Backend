package project.airbnb.clone.controller.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.WithMockMember;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.dto.payment.PaymentConfirmReqDto;
import project.airbnb.clone.dto.payment.PaymentResDto;
import project.airbnb.clone.dto.payment.SavePaymentReqDto;
import project.airbnb.clone.service.payment.PaymentService;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest extends RestDocsTestSupport {

    private static final String PAYMENT_API_TAG = "Payment API";

    @MockitoBean PaymentService paymentService;

    @Test
    @DisplayName("결제 정보 임시 저장")
    void savePayment() throws Exception {
        //given
        SavePaymentReqDto request = new SavePaymentReqDto("e6564672-8221-4908-953f-2a35243a10aa", 100_000);

        //when
        //then
        mockMvc.perform(post("/api/payments/save")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(request)))
               .andExpectAll(
                       handler().handlerType(PaymentController.class),
                       handler().methodName("savePayment"),
                       status().isOk()
               )
               .andDo(
                       document("save-payment",
                               resource(
                                       builder()
                                               .tag(PAYMENT_API_TAG)
                                               .summary("결제 정보 임시 저장")
                                               .description("결제를 요청하기 전에 서버에 임시로 저장합니다. 결제 승인 요청 전에 반드시 요청해야 하며, 결제 요청과 승인 사이에 데이터 무결성을 위함입니다.")
                                               .requestFields(
                                                       fieldWithPath("orderId")
                                                               .description("주문번호, 주문할 결제를 식별하는 역할 (영문 대소문자, 숫자, 특수문자(-, _)로 이루어진 6자 이상 64자 이하의 문자열)")
                                                               .type(STRING),
                                                       fieldWithPath("amount")
                                                               .description("결제할 금액")
                                                               .type(NUMBER)
                                               )
                                               .requestSchema(schema("SavePaymentRequest"))
                                               .build()
                               )
                       ));
    }

    @Test
    @DisplayName("결제 승인 요청")
    @WithMockMember
    void confirmPayment() throws Exception {
        //given
        PaymentConfirmReqDto request = new PaymentConfirmReqDto(
                "e6564672-8221-4908-953f-2a35243a10aa", "6e91a118-e347-4d07-965d-6e84d7bfd227", 100_000, 1L
        );
        PaymentResDto response = new PaymentResDto("https://receipt-exmaple.com");
        BDDMockito.given(paymentService.confirmPayment(any(), anyLong())).willReturn(response);

        //when
        //then
        mockMvc.perform(post("/api/payments/confirm")
                       .header(AUTHORIZATION, "Bearer {access-token}")
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .content(creatJson(request)))
               .andExpectAll(
                       handler().handlerType(PaymentController.class),
                       handler().methodName("confirmPayment"),
                       status().isOk(),
                       jsonPath("$.receiptUrl").value(response.receiptUrl())
               )
               .andDo(
                       document("confirm-payment",
                               resource(
                                       builder()
                                               .tag(PAYMENT_API_TAG)
                                               .summary("결제 승인 요청")
                                               .description("실제 토스 결제 승인 API를 요청합니다")
                                               .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                               .requestFields(
                                                       fieldWithPath("paymentKey")
                                                               .description("결제의 키값, 결제를 식별하는 역할")
                                                               .type(STRING),
                                                       fieldWithPath("orderId")
                                                               .description("주문번호, 주문할 결제를 식별하는 역할")
                                                               .type(STRING),
                                                       fieldWithPath("amount")
                                                               .description("결제된 금액")
                                                               .type(NUMBER),
                                                       fieldWithPath("reservationId")
                                                               .description("결제 처리된 예약 ID")
                                                               .type(NUMBER)
                                               )
                                               .responseFields(
                                                       fieldWithPath("receiptUrl")
                                                               .description("영수증 주소")
                                                               .type(STRING)
                                               )
                                               .requestSchema(schema("ConfirmPaymentRequest"))
                                               .responseSchema(schema("ConfirmPaymentResponse"))
                                               .build()
                               )
                       ));
    }
}