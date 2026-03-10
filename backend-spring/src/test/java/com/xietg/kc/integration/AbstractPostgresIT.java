package com.xietg.kc.integration;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractPostgresIT {

  @Container
  public static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("kc_test")
          .withUsername("kc")
          .withPassword("kc");

  @DynamicPropertySource
  static void register(DynamicPropertyRegistry r) {
      r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
      r.add("spring.datasource.username", POSTGRES::getUsername);
      r.add("spring.datasource.password", POSTGRES::getPassword);
  }
}
