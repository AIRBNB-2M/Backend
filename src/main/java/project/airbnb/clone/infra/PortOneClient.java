package project.airbnb.clone.infra;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortOneClient {

	private final RestTemplate restTemplate;

    @Value("${portone.api-key}")
    private String apiKey;
    @Value("${portone.api-secret}")
    private String apiSecret;

    private final AtomicReference<TokenCache> cacheRef = new AtomicReference<>();

    private String token() {
        var cache = cacheRef.get();
        long now = Instant.now().getEpochSecond();
        if (cache != null && cache.expiresAt > now + 30) return cache.token;

        var body = Map.of("imp_key", apiKey, "imp_secret", apiSecret);
        var resp = restTemplate.postForEntity("https://api.iamport.kr/users/getToken", body, TokenResp.class).getBody();
        if (resp == null || resp.response == null || resp.response.access_token == null)
            throw new IllegalStateException("포트원 토큰 발급 실패");

        cacheRef.set(new TokenCache(resp.response.access_token, now + (resp.response.expired_at == null ? 300 : resp.response.expired_at)));
        return cacheRef.get().token;
    }

    public void prepare(String merchantUid, int amount) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("Authorization", token());
        var req = new HttpEntity<>(Map.of("merchant_uid", merchantUid, "amount", amount), h);
        restTemplate.exchange("https://api.iamport.kr/payments/prepare", HttpMethod.POST, req, Void.class);
    }

    public PaymentDetail getPayment(String impUid) {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", token());
        var resp = restTemplate.exchange("https://api.iamport.kr/payments/" + impUid, HttpMethod.GET, new HttpEntity<>(h), PaymentDetailResp.class).getBody();
        if (resp == null || resp.response == null) throw new IllegalStateException("결제 조회 실패");
        return resp.response;
    }

    // DTOs
    @Data static class TokenResp { private Token response; }
    @Data static class Token { private String access_token; private Long expired_at; }

    @Data public static class PaymentDetailResp { private PaymentDetail response; }
    @Data public static class PaymentDetail {
        private String imp_uid;
        private String merchant_uid;
        private Integer amount;
        private String status;      // "paid" 등
        private String pay_method;  // "card","kakao","naver"
        private Long paid_at;       // epoch sec
    }

    record TokenCache(String token, long expiresAt) {}
}
