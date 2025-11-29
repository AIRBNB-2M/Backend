package project.airbnb.clone.repository.dto.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "email:verify", timeToLive = 3600)
public class EmailVerification {

    @Id
    private String token;

    private Long memberId;
}
