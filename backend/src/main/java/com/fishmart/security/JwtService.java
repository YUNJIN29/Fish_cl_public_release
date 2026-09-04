package com.fishmart.security;

import com.fishmart.config.AppProperties;
import com.fishmart.user.RoleName;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;

@Service
public class JwtService {
  private final AppProperties props;
  private final SecretKey key;

  public JwtService(AppProperties props) {
    this.props = props;
    this.key = Keys.hmacShaKeyFor(props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String createAccessToken(Long userId, String username, Set<RoleName> roles) {
    Instant now = Instant.now();
    Instant exp = now.plus(props.getJwt().getAccessTokenMinutes(), ChronoUnit.MINUTES);

    return Jwts.builder()
        .issuer(props.getJwt().getIssuer())
        .subject(String.valueOf(userId))
        .claim("username", username)
        .claim("roles", roles.stream().map(Enum::name).toList())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}

