package com.fishmart.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionConfigValidator {
  private final AppProperties properties;
  private final Environment environment;

  public ProductionConfigValidator(AppProperties properties, Environment environment) {
    this.properties = properties;
    this.environment = environment;
  }

  @PostConstruct
  void validate() {
    String secret = properties.getJwt().getSecret();
    if (secret == null || secret.length() < 32 || secret.toLowerCase().contains("replace")) {
      throw new IllegalStateException("生产环境 JWT_SECRET 必须是至少 32 位的随机字符串");
    }
    String password = environment.getProperty("spring.datasource.password", "");
    if (password.isBlank() || password.toLowerCase().contains("replace")) {
      throw new IllegalStateException("生产环境数据库密码不能为空或使用示例值");
    }
  }
}
