package project.airbnb.clone.service.guest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.common.exceptions.ImageUploadException;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.repository.jpa.GuestRepository;
import project.airbnb.clone.service.s3.S3Uploader;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestProfileImageUploadService {

    private final GuestRepository guestRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public void upload(Long guestId, String imageUrl) {
        Guest guest = guestRepository.getGuestById(guestId);

        String key = String.format("guests/%s", UUID.randomUUID());

        try {
            guest.setProfileUrl(s3Uploader.uploadImage(imageUrl, key));
            log.debug("Succeed to upload image to S3: guestId={}", guestId);
        } catch (ImageUploadException e) {
            log.warn("failed image upload for guestId={}. Continue without profile image.", guestId, e);
        }
    }
}
