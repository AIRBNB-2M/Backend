package project.airbnb.clone.common.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ========== 공통 ==========
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON-001", "잘못된 입력값입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-002", "서버 오류가 발생했습니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON-003", "접근 권한이 없습니다."),

    // ========== 사용자 ==========
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-001", "사용자를 찾을 수 없습니다"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER-002", "이미 존재하는 이메일입니다"),

    // ========== 숙소 ==========
    ACCOMMODATION_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOMMODATION-001", "숙소를 찾을 수 없습니다"),

    // ========== 인증 ==========
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUT-001", "인증이 필요합니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUT-002", "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUT-003", "토큰이 만료되었습니다"),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "AUT-004", "이메일 또는 비밀번호가 일치하지 않습니다"),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "AUT-006", "토큰 형식이 올바르지 않습니다"),
    BLACKLISTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUT-007", "이미 로그아웃된 토큰입니다"),

    // ========== Entity Not Found (404) ==========
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "ENT-001", "요청한 데이터를 찾을 수 없습니다"),

    // ========== 채팅 ==========
    ALREADY_ACTIVE_CHAT(HttpStatus.BAD_REQUEST, "CHT-002", "이미 활성화된 채팅방이 있습니다"),
    ALREADY_REQUEST_SENT(HttpStatus.BAD_REQUEST, "CHT-003", "이미 채팅 요청을 보냈습니다"),
    SAME_PARTICIPANT(HttpStatus.BAD_REQUEST, "CHT-004", "자기 자신에게 채팅을 요청할 수 없습니다"),
    PARTICIPANT_LEFT(HttpStatus.BAD_REQUEST, "CHT-005", "상대방이 채팅방을 나갔습니다"),

    // ========== 예약 & 결제 ==========
    ALREADY_RESERVED(HttpStatus.CONFLICT, "RESERVATION-001", "먼저 처리된 예약이 존재합니다"),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT-001", "임시 저장된 결제 정보를 찾을 수 없습니다"),
    NOT_EQUALS_AMOUNT(HttpStatus.BAD_REQUEST, "PAYMENT-001", "임시 저장된 결제 정보와 일치하지 않습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
