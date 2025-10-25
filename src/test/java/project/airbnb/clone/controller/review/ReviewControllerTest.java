package project.airbnb.clone.controller.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import project.airbnb.clone.WithMockGuest;
import project.airbnb.clone.controller.RestDocsTestSupport;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.review.MyReviewResDto;
import project.airbnb.clone.service.review.ReviewService;

import java.time.LocalDate;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest extends RestDocsTestSupport {

    private static final String REVIEW_API_TAG = "Review API";

    @MockitoBean ReviewService reviewService;

    @Test
    @DisplayName("등록한 후기 목록 조회")
    @WithMockGuest
    void getMyReviews() throws Exception {
        //given
        LocalDate now = LocalDate.now();
        List<MyReviewResDto> dtos = List.of(
                new MyReviewResDto(1L, 1L, "https://example-a.com", "title-A", "content-A", 3.0, now.minusDays(14)),
                new MyReviewResDto(2L, 2L, "https://example-b.com", "title-B", "content-B", 4.0, now.minusDays(10)),
                new MyReviewResDto(3L, 3L, "https://example-c.com", "title-C", "content-C", 4.5, now.minusDays(7))
        );
        PageResponseDto<MyReviewResDto> response = PageResponseDto.<MyReviewResDto>builder()
                                                                  .contents(dtos)
                                                                  .pageNumber(0)
                                                                  .pageSize(10)
                                                                  .total(dtos.size())
                                                                  .build();
        given(reviewService.getMyReviews(anyLong(), any()))
                .willReturn(response);

        //when
        //then
        mockMvc.perform(get("/api/reviews/me")
                       .header(AUTHORIZATION, "Bearer {access-token}")
                       .param("page", "0")
                       .param("size", "10")
               )
               .andExpectAll(
                       handler().handlerType(ReviewController.class),
                       handler().methodName("getMyReviews"),
                       status().isOk(),
                       jsonPath("$.contents.length()").value(dtos.size())
               )
               .andDo(document("get-my-reviews",
                       resource(
                               builder()
                                       .tag(REVIEW_API_TAG)
                                       .summary("등록한 후기 목록 조회")
                                       .requestHeaders(headerWithName(AUTHORIZATION).description("Bearer {액세스 토큰}"))
                                       .queryParameters(
                                               parameterWithName("size").optional().description("페이지 크기"),
                                               parameterWithName("page").optional().description("페이지 번호 (0-index)")
                                       )
                                       .responseFields(
                                               fieldWithPath("contents")
                                                       .type(ARRAY)
                                                       .description("검색 페이지 데이터"),
                                               fieldWithPath("hasPrev")
                                                       .type(BOOLEAN)
                                                       .description("이전 페이지 존재 여부"),
                                               fieldWithPath("hasNext")
                                                       .type(BOOLEAN)
                                                       .description("다음 페이지 존재 여부"),
                                               fieldWithPath("totalCount")
                                                       .type(NUMBER)
                                                       .description("검색된 전체 데이터 개수"),
                                               fieldWithPath("prevPage")
                                                       .type(NUMBER)
                                                       .description("이전 페이지 번호 (0-index, 없으면 -1)"),
                                               fieldWithPath("nextPage")
                                                       .type(NUMBER)
                                                       .description("다음 페이지 번호 (0-index, 없으면 -1)"),
                                               fieldWithPath("totalPage")
                                                       .type(NUMBER)
                                                       .description("총 페이지 개수"),
                                               fieldWithPath("current")
                                                       .type(NUMBER)
                                                       .description("현재 페이지 번호 (0-index)"),
                                               fieldWithPath("size")
                                                       .type(NUMBER)
                                                       .description("페이지 크기"),
                                               fieldWithPath("contents[].reviewId")
                                                       .type(NUMBER)
                                                       .description("후기 ID"),
                                               fieldWithPath("contents[].accommodationId")
                                                       .type(NUMBER)
                                                       .description("숙소 ID"),
                                               fieldWithPath("contents[].thumbnailUrl")
                                                       .type(STRING)
                                                       .description("숙소 썸네일 URL"),
                                               fieldWithPath("contents[].title")
                                                       .type(STRING)
                                                       .description("숙소 제목"),
                                               fieldWithPath("contents[].content")
                                                       .type(STRING)
                                                       .description("리뷰 내용"),
                                               fieldWithPath("contents[].rate")
                                                       .type(NUMBER)
                                                       .description("리뷰 평점"),
                                               fieldWithPath("contents[].createdDate")
                                                       .type(STRING)
                                                       .description("리뷰 등록일")
                                       )
                                       .responseSchema(schema("MyReviewResponse"))
                                       .build()
                       ))
               );
    }
}