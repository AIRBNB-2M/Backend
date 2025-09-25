package project.airbnb.clone.common.events.guest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.airbnb.clone.service.guest.GuestProfileImageUploadService;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuestImageUploadListener {

    private final GuestProfileImageUploadService profileImageUploadService;

    @EventListener
    public void handleGuestImageUploadEvent(GuestImageUploadEvent event) {
        log.debug("GuestImageUploadListener.handleGuestImageUploadEvent");
        profileImageUploadService.upload(event.guestId(), event.imageUrl());
    }

    @EventListener
    public void handleGuestImageUploadEvent(GuestProfileImageChangedEvent event) {
        log.debug("GuestImageUploadListener.GuestProfileImageChangedEvent");
        profileImageUploadService.uploadAndDeleteOrigin(event.guestId(), event.oldImageUrl(), event.newImageFile());
    }
}
