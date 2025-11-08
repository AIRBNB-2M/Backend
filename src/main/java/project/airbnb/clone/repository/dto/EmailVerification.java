package project.airbnb.clone.repository.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "email:verify", timeToLive = 3600)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailVerification {

    @Id
    String token;

    Long guestId;
}
