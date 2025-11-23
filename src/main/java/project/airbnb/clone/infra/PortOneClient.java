package project.airbnb.clone.infra;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class PortOneClient {

    private final org.springframework.web.client.RestClient restClient;

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
        var resp = restClient.post()
                .uri("https://api.iamport.kr/users/getToken")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(TokenResp.class);
        if (resp == null || resp.response == null || resp.response.access_token == null)
            throw new IllegalStateException("포트원 토큰 발급 실패");

        long expiresAt = (resp.response.expired_at == null)
                ? now + 300
                : resp.response.expired_at;

        cacheRef.set(new TokenCache(resp.response.access_token, expiresAt));
        return cacheRef.get().token;
    }

    public void prepare(String merchantUid, int amount) {
        var resp = restClient.post()
                .uri("https://api.iamport.kr/payments/prepare")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("merchant_uid", merchantUid, "amount", amount))
                .retrieve()
                .toBodilessEntity();
    }

    public PaymentDetail getPayment(String impUid) {
        var resp = restClient.get()
                .uri("https://api.iamport.kr/payments/{impUid}", impUid)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token())
                .retrieve()
                .body(PaymentDetailResp.class);
        if (resp == null || resp.response == null) throw new IllegalStateException("결제 조회 실패");
        return resp.response;
    }

    // DTOs 동일
    @Data static class TokenResp { private Token response; }
    @Data static class Token { private String access_token; private Long expired_at; }

    @Data public static class PaymentDetailResp { private PaymentDetail response; }
    @Data public static class PaymentDetail {
        private String imp_uid;
        private String merchant_uid;
        private Integer amount;
        private String status;
        private String pay_method;
        private Long paid_at;
    }

    record TokenCache(String token, long expiresAt) {}
}

