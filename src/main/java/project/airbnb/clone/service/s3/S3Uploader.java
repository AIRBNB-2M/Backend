package project.airbnb.clone.service.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import project.airbnb.clone.common.exceptions.ImageUploadException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.BufferedInputStream;
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

    @Value("${cloudflare.r2.endpoint}")
    private String bucketPublicUrl;

    /**
     * @param imageUrl R2에 저장할 이미지 주소
     * @param key R2에 저장이 될 이미지 주소 이름("/"로 폴더처럼 구분 가능)
     * @return R2에 저장된 이미지 주소
     */
    public String uploadImage(String imageUrl, String key) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000); //10s
            connection.setReadTimeout(30000); //30s

            long contentLength = connection.getContentLengthLong();
            String contentType = connection.getContentType();

            try (InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
                RequestBody requestBody = RequestBody.fromInputStream(inputStream, contentLength);

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                                                    .bucket(bucketName)
                                                                    .key(key)
                                                                    .contentType(contentType)
                                                                    .build();

                s3Client.putObject(putObjectRequest, requestBody);
            }

            return bucketPublicUrl + "/" + key;

        } catch (Exception e) {
            log.error("Failed to upload image to S3: key={}", key, e);
            throw new ImageUploadException("Failed to upload image to S3", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
