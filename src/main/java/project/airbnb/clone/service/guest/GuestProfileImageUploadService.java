package project.airbnb.clone.service.guest;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
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
        uploadProfileImage(guestId, key -> s3Uploader.uploadImage(imageUrl, key));
    }

    @Transactional
    public void uploadAndDeleteOrigin(Long guestId, String oldImageUrl, MultipartFile newImageFile) {
        uploadProfileImage(guestId, key -> newImageFile != null ? s3Uploader.uploadImage(newImageFile, key) : null);

        if (StringUtils.hasText(oldImageUrl)) {
            s3Uploader.deleteFile(oldImageUrl);
        }
    }

    private void uploadProfileImage(Long guestId, FileUploadFunction uploadFunction) {
        Guest guest = guestRepository.findById(guestId)
                                     .orElseThrow(() -> new EntityNotFoundException("Guest with id " + guestId + "cannot be found"));
        String key = String.format("guests/%s", UUID.randomUUID());

        try {
            guest.updateProfileUrl(uploadFunction.upload(key));
            log.debug("Succeed to upload image to S3: guestId={}", guest.getId());
        } catch (ImageUploadException e) {
            log.warn("Failed image upload for guestId={}. Continue without profile image.", guest.getId(), e);
        }
    }

    @FunctionalInterface
    private interface FileUploadFunction {
        String upload(String key) throws ImageUploadException;
    }
}
