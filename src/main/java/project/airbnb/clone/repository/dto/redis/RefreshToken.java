package project.airbnb.clone.repository.dto.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "jwt:refreshToken")
public class RefreshToken {

    @Id
    private String token;

    private Long memberId;

    @TimeToLive
    private Long ttl;
}
