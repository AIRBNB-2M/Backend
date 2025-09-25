package project.airbnb.clone.common.events.guest;

import org.springframework.web.multipart.MultipartFile;

public record GuestProfileImageChangedEvent(
        Long guestId,
        String oldImageUrl,
        MultipartFile newImageFile) {
}