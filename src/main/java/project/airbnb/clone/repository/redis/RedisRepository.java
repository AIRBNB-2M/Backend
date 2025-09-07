package project.airbnb.clone.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class RedisRepository {

    private final StringRedisTemplate redisTemplate;

    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void setValue(String key, String value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    public void addSet(String key, Collection<String> values) {
        addSet(key, values, null);
    }

    public void addSet(String key, Collection<String> values, Duration duration) {
        if (values == null || values.isEmpty()) {
            return;
        }
        redisTemplate.opsForSet().add(key, values.toArray(String[]::new));

        if (duration != null) {
            redisTemplate.expire(key, duration);
        }
    }

    public boolean isMemberOfSet(String key, String value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}
