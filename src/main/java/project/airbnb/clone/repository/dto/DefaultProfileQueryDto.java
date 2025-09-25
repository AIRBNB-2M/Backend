package project.airbnb.clone.repository.dto;

import java.time.LocalDateTime;

public record DefaultProfileQueryDto(
        String name,
        String profileImageUrl,
        LocalDateTime createdDateTime,
        String aboutMe,
        boolean isEmailVerified) {
}
