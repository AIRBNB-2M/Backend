package project.airbnb.clone.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
@Profile("h2")
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @PostConstruct
    public void startRedis()  {
        try {
            redisServer = RedisServer.newRedisServer()
                                     .port(redisPort)
                                     .build();
            redisServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}