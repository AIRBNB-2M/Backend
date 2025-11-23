package project.airbnb.clone.dto.member;

import project.airbnb.clone.repository.dto.DefaultProfileQueryDto;

import java.time.LocalDate;

public record DefaultProfileResDto(
        String name,
        String profileImageUrl,
        LocalDate createdDate,
        String aboutMe,
        boolean isEmailVerified) {

    public static DefaultProfileResDto from(DefaultProfileQueryDto queryDto) {
        return new DefaultProfileResDto(
                queryDto.name(),
                queryDto.profileImageUrl(),
                queryDto.createdDateTime().toLocalDate(),
                queryDto.aboutMe(),
                queryDto.isEmailVerified()
        );
    }
}
