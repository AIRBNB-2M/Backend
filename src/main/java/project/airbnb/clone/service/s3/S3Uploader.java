package project.airbnb.clone.service.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import project.airbnb.clone.common.exceptions.ImageUploadException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucketName;

    @Value("${cloudflare.r2.public-url}")
    private String bucketPublicUrl;

    /**
     * @param imageUrl R2에 저장할 이미지 주소
     * @param key R2에 저장이 될 이미지 주소 이름("/"로 폴더처럼 구분 가능)
     * @return R2에 저장된 이미지 주소
     */
    public String uploadImage(String imageUrl, String key) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000); //10s
            connection.setReadTimeout(30000); //30s

            try (InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
                long contentLength = connection.getContentLengthLong();
                String contentType = connection.getContentType();

                return uploadToS3(inputStream, contentLength, contentType, key);
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            log.error("Failed to upload image from URL to S3: key={}", key, e);
            throw new ImageUploadException("Failed to upload image to S3", e);
        }
    }

    public String uploadImage(MultipartFile file, String key) {
        try (InputStream inputStream = file.getInputStream()) {
            long contentLength = file.getSize();
            String contentType = file.getContentType();
            return uploadToS3(inputStream, contentLength, contentType, key);
        } catch (IOException e) {
            log.error("Failed to upload MultipartFile to S3: key={}", key, e);
            throw new ImageUploadException("Failed to upload image to S3", e);
        }
    }

    public void deleteFile(String oldImageUrl) {
        try {
            String prefix = bucketPublicUrl + "/";

            if (!oldImageUrl.startsWith(prefix)) {
                log.warn("Invalid image url. Skip deletion. url={}", oldImageUrl);
                return;
            }

            String key = oldImageUrl.substring(prefix.length());

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                                                                         .bucket(bucketName)
                                                                         .key(key)
                                                                         .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.debug("Deleted file from R2: bucket={}, key={}", bucketName, key);

        } catch (Exception e) {
            log.error("Failed to delete file from R2: url={}", oldImageUrl, e);
        }
    }

    private String uploadToS3(InputStream inputStream, long contentLength, String contentType, String key) {
        try {
            RequestBody requestBody = RequestBody.fromInputStream(inputStream, contentLength);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                                                .bucket(bucketName)
                                                                .key(key)
                                                                .contentType(contentType)
                                                                .build();

            s3Client.putObject(putObjectRequest, requestBody);
            return bucketPublicUrl + "/" + key;
        } catch (Exception e) {
            log.error("Failed to upload input stream to S3: key={}", key, e);
            throw new ImageUploadException("Failed to upload image to S3", e);
        }
    }
}
