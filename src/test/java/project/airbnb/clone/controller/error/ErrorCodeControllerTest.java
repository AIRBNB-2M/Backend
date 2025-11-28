package project.airbnb.clone.controller.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import project.airbnb.clone.controller.RestDocsTestSupport;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ErrorCodeController.class)
class ErrorCodeControllerTest extends RestDocsTestSupport {

    private static final String ERROR_API_TAG = "Error API";

    @Test
    @DisplayName("정의된 예외 목록 조회")
    void getErrors() throws Exception {
        //given

        //when
        //then
        mockMvc.perform(get("/api/errors"))
                .andExpectAll(
                        handler().handlerType(ErrorCodeController.class),
                        handler().methodName("getErrors"),
                        status().isOk()
                )
                .andDo(document("get-errors",
                                resource(
                                        builder()
                                                .tag(ERROR_API_TAG)
                                                .summary("정의된 예외 목록 조회")
                                                .responseFields(
                                                        fieldWithPath("[].status")
                                                                .type(NUMBER)
                                                                .description("HTTP 상태 코드"),
                                                        fieldWithPath("[].message")
                                                                .type(STRING)
                                                                .description("오류 메시지"),
                                                        fieldWithPath("[].errorCode")
                                                                .type(STRING)
                                                                .description("서버 오류 코드"),
                                                        fieldWithPath("[].errors")
                                                                .type(OBJECT)
                                                                .optional()
                                                                .description("JSON 입력 값 오류")
                                                )
                                                .responseSchema(schema("ErrorResponse"))
                                                .build()
                                )
                        ));
    }
}