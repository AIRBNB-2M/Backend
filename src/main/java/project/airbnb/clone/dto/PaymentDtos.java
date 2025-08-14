package project.airbnb.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PaymentDtos {

	@Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PrepareRequest {
        private Long reservationId;     // 결제와 연결할 예약 ID
        private String merchantUid;     // 주문번호(멱등키)
        private Integer amount;         // 결제 금액
    }
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PrepareResponse {
        private String merchantUid;
        private Integer amount;
        private String status;          // READY
    }
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CompleteRequest {
        private String impUid;          // 포트원 imp_uid
        private String merchantUid;     // 우리 주문번호
    }
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CompleteResponse {
        private String merchantUid;
        private String impUid;
        private Integer amount;
        private String status;          // PAID/FAILED
    }
}
