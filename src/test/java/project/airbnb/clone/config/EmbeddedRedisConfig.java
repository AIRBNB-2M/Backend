package project.airbnb.clone.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;

@Configuration
@Profile("h2")
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @PostConstruct
    public void startRedis() {
        try {
            // 포트가 이미 사용 중인지 확인하고, 사용 중이면 다른 포트를 찾음
            int availablePort = isPortInUse(redisPort) ? findAvailablePort() : redisPort;

            redisServer = RedisServer.newRedisServer()
                                     .port(availablePort)
                                     .build();
            redisServer.start();

            // 실제 사용된 포트를 시스템 프로퍼티로 설정
            System.setProperty("spring.data.redis.port", String.valueOf(availablePort));

        } catch (IOException e) {
            throw new RuntimeException("Failed to start embedded Redis server", e);
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            try {
                redisServer.stop();
            } catch (IOException e) {
                // 로그만 출력하고 예외는 무시
                System.err.println("Failed to stop Redis server: " + e.getMessage());
            }
        }
    }

    private boolean isPortInUse(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private int findAvailablePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }
}