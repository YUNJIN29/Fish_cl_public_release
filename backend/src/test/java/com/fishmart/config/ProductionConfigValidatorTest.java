package com.fishmart.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionConfigValidatorTest {
  @Test
  void rejectsWeakProductionSecrets() {
    AppProperties properties = new AppProperties();
    properties.getJwt().setSecret("short");
    MockEnvironment environment = new MockEnvironment().withProperty("spring.datasource.password", "");
    ProductionConfigValidator validator = new ProductionConfigValidator(properties, environment);
    assertThatThrownBy(validator::validate).isInstanceOf(IllegalStateException.class).hasMessageContaining("JWT_SECRET");
  }
}
