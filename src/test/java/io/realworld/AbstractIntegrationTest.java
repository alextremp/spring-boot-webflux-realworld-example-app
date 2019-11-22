package io.realworld;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("integration-test")
@SpringBootTest(
    classes = {ConduitSpringBootApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {AbstractIntegrationTest.Initializer.class})
public abstract class AbstractIntegrationTest {

  private static final PostgreSQLContainer POSTGRE_SQL_CONTAINER =
      (PostgreSQLContainer) new PostgreSQLContainer("postgres:11.1")
          .withDatabaseName("conduit")
          .withUsername("tester")
          .withPassword("tester")
          .withReuse(true);

  static {
    /*
      Sharing same container between multiple Test classes does not work properly,
      instead of using @TestContainers and @Container annotations, the only way to make it work
      is to start a shared container statically on initialization.
      @see https://github.com/testcontainers/testcontainers-java/issues/417
     */
    POSTGRE_SQL_CONTAINER.start();
  }

  static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertyValues.of(
          "spring.datasource.url=" + POSTGRE_SQL_CONTAINER.getJdbcUrl(),
          "spring.datasource.username=" + POSTGRE_SQL_CONTAINER.getUsername(),
          "spring.datasource.password=" + POSTGRE_SQL_CONTAINER.getPassword())
          .applyTo(configurableApplicationContext.getEnvironment());
    }
  }

  private String address;

  @Autowired
  private TestRestTemplate restTemplate;
  @Value("${local.server.port}")
  private Integer port;

  @BeforeEach
  public void setUp() {
    address = "http://localhost:" + port;
  }
}
