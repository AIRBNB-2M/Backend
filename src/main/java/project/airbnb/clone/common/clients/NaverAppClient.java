package project.airbnb.clone.common.clients;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Naver REST API Http Client
 */
@HttpExchange
public interface NaverAppClient {

    /**
     * 네이버 로그아웃 요청 API
     * <hr>
     * <a href="https://developers.naver.com/docs/login/api/api.md#6--%EC%98%88%EC%8B%9C">REST API Docs</a>
     *
     * @param accessToken 네이버 액세스 토큰
     * @return 삭제 처리된 토큰 값, 성공 시 "success", (에러 발생 시) 에러 내용
     */
    @GetExchange("?grant_type=delete&client_id={client_id}&client_secret={client_secret}&service_provider=NAVER")
    NaverResponse logout(@RequestParam("access_token") String accessToken);

    record NaverResponse(
            String access_token,
            String result,
            Integer expires_in,
            String error,
            String error_description) {
    }
}
