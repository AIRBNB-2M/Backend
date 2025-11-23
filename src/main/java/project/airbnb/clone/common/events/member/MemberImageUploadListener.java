package project.airbnb.clone.common.events.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.airbnb.clone.service.member.MemberProfileImageUploadService;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberImageUploadListener {

    private final MemberProfileImageUploadService profileImageUploadService;

    @EventListener
    public void handleMemberImageUploadEvent(MemberImageUploadEvent event) {
        profileImageUploadService.upload(event.memberId(), event.imageUrl());
    }

    @EventListener
    public void handleMemberImageUploadEvent(MemberProfileImageChangedEvent event) {
        profileImageUploadService.uploadAndDeleteOrigin(event.memberId(), event.oldImageUrl(), event.newImageFile());
    }
}
