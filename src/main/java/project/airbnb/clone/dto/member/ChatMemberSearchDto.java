package project.airbnb.clone.dto.member;

import java.time.LocalDateTime;

public record ChatMemberSearchDto(
        Long id,
        String name,
        LocalDateTime createdDateTime,
        String profileImageUrl) {
}
