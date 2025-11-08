package project.airbnb.clone.repository.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "jwt:blacklist")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlacklistedToken {

    @Id
    String token;

    @TimeToLive
    Long ttl;
}
