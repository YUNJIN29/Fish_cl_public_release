package com.fishmart.security;

import com.fishmart.config.AppProperties;
import com.fishmart.user.RoleName;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

  @Test
  void createsAccessTokenThatRemainsValidForSevenDaysByDefault() {
    AppProperties properties = new AppProperties();
    properties.getJwt().setIssuer("aquatic-test");
    properties.getJwt().setSecret("0123456789abcdef0123456789abcdef");

    JwtService service = new JwtService(properties);
    Claims claims = service.parse(service.createAccessToken(1L, "tester", Set.of(RoleName.USER)));

    Duration validity = Duration.between(claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant());
    assertThat(validity).isEqualTo(Duration.ofDays(7));
  }
}
