package com.example.phoneWallet;

import com.example.phoneWallet.Repository.UserRepository;
import com.example.phoneWallet.entity.User;
import com.example.phoneWallet.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.fail-fast=false",
        "spring.kafka.bootstrap-servers=localhost:65535",
        "jwt.secretKey=12345678901234567890123456789012",
        "wallet.topup.webhook-secret=12345678901234567890123456789012",
        "wallet.topup.demo-enabled=true",
        "wallet.kafka.topics.enabled=false",
        "wallet.scheduling.enabled=false"
})
class InfrastructureIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("phoneWallet")
            .withUsername("wallet")
            .withPassword("walletpass");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired UserRepository userRepository;

    @Test
    void databaseSchemaEnforcesUniqueUsername() {
        User user = new User("container-user", "9876543210", "encoded", Role.USER);
        userRepository.saveAndFlush(user);
        assertTrue(userRepository.existsByUsername("container-user"));
    }
}
