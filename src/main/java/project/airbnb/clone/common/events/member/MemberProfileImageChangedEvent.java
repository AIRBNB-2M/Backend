package project.airbnb.clone.common.events.member;

import org.springframework.web.multipart.MultipartFile;

public record MemberProfileImageChangedEvent(
        Long memberId,
        String oldImageUrl,
        MultipartFile newImageFile) {
}