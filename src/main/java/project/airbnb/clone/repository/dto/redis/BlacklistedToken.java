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
@RedisHash(value = "jwt:blacklist")
public class BlacklistedToken {

    @Id
    private String token;

    @TimeToLive
    private Long ttl;
}
