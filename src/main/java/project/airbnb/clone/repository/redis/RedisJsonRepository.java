package project.airbnb.clone.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisJsonRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public <T> void save(String key, T value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @SuppressWarnings("unchecked")
    public <T> T find(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return null;
        return clazz.isInstance(value) ? (T) value : null;
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    public void addToSet(String key, String value, Duration ttl) {
        redisTemplate.opsForSet().add(key, value);
        if (ttl != null) redisTemplate.expire(key, ttl);
    }

    public Set<String> getSet(String key) {
        Set<Object> result = redisTemplate.opsForSet().members(key);
        if (result == null) return Collections.emptySet();
        return result.stream().map(Object::toString).collect(Collectors.toSet());
    }
}
