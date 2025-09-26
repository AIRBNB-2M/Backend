package project.airbnb.clone;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class DynamicTestSupport extends IntegrationTestSupport {

    private static final MySQLContainer<?> MYSQL_CONTAINER;
    private static final GenericContainer<?> REDIS_CONTAINER;

    static {
        String activeProfiles = System.getProperty("spring.profiles.active", "");
        boolean useContainers = activeProfiles.contains("testcontainers");

        if (useContainers) {
            MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0.35")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

            REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7.2.4"))
                    .withExposedPorts(6379);

            MYSQL_CONTAINER.start();
            REDIS_CONTAINER.start();
        } else {
            MYSQL_CONTAINER = null;
            REDIS_CONTAINER = null;
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String activeProfiles = System.getProperty("spring.profiles.active", "");

        if (activeProfiles.contains("testcontainers") && MYSQL_CONTAINER != null) {
            // TestContainers 설정
            registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
            registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
            registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
            registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
            registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
        } else {
            // H2 + Embedded Redis 설정
            registry.add("spring.datasource.url",
                    () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
            registry.add("spring.datasource.username", () -> "sa");
            registry.add("spring.datasource.password", () -> "");
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
            registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
            registry.add("spring.data.redis.host", () -> "localhost");
            registry.add("spring.data.redis.port", () -> 6377);
        }
    }
}
