package project.airbnb.clone;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class TestContainersConfig {

    @Container
    public static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0.35")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    public static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7.2.4"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // MySQL
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        // Redis
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
    }
}
