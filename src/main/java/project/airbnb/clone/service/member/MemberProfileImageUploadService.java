package project.airbnb.clone.service.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import project.airbnb.clone.common.exceptions.ImageUploadException;
import project.airbnb.clone.common.exceptions.factory.MemberExceptions;
import project.airbnb.clone.entity.member.Member;
import project.airbnb.clone.repository.jpa.MemberRepository;
import project.airbnb.clone.service.s3.S3Uploader;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberProfileImageUploadService {

    private final S3Uploader s3Uploader;
    private final MemberRepository memberRepository;

    @Transactional
    public void upload(Long memberId, String imageUrl) {
        uploadProfileImage(memberId, key -> s3Uploader.uploadImage(imageUrl, key));
    }

    @Transactional
    public void uploadAndDeleteOrigin(Long memberId, String oldImageUrl, MultipartFile newImageFile) {
        uploadProfileImage(memberId, key -> newImageFile != null ? s3Uploader.uploadImage(newImageFile, key) : null);

        if (StringUtils.hasText(oldImageUrl)) {
            s3Uploader.deleteFile(oldImageUrl);
        }
    }

    private void uploadProfileImage(Long memberId, FileUploadFunction uploadFunction) {
        Member member = memberRepository.findById(memberId)
                                        .orElseThrow(() -> MemberExceptions.notFoundById(memberId));
        String key = String.format("members/%s", UUID.randomUUID());

        try {
            member.updateProfileUrl(uploadFunction.upload(key));
            log.debug("Succeed to upload image to S3: memberId={}", member.getId());
        } catch (ImageUploadException e) {
            log.warn("Failed image upload for memberId={}. Continue without profile image.", member.getId(), e);
        }
    }

    @FunctionalInterface
    private interface FileUploadFunction {
        String upload(String key) throws ImageUploadException;
    }
}
