package project.airbnb.clone.common.clients;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import static project.airbnb.clone.common.jwt.JwtProperties.AUTHORIZATION_HEADER;

/**
 * Kakao REST API Http Client
 */
@HttpExchange
public interface KakaoAppClient {

    /**
     * 카카오 로그아웃 요청 API
     * <hr>
     * <a href="https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#logout">REST API Docs</a>
     * @param accessToken 카카오 액세스 토큰 (Bearer 포함)
     * @return 로그아웃된 사용자의 ID
     */
    @PostExchange("/logout")
    KakaoIdResponse logout(@RequestHeader(AUTHORIZATION_HEADER) String accessToken);

    record KakaoIdResponse(Long id) {}
}
