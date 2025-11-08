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
@RedisHash(value = "jwt:refreshToken")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshToken {

    @Id
    String token;

    Long guestId;

    @TimeToLive
    Long ttl;
}
