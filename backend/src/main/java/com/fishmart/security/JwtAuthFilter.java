package com.fishmart.security;

import com.fishmart.user.RoleName;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring(7);
      try {
        Claims claims = jwtService.parse(token);
        Long userId = Long.valueOf(claims.getSubject());
        String username = String.valueOf(claims.get("username"));
        @SuppressWarnings("unchecked")
        List<String> rolesRaw = (List<String>) claims.get("roles");
        Set<RoleName> roles = rolesRaw == null ? Set.of() : rolesRaw.stream().map(RoleName::valueOf).collect(Collectors.toSet());

        List<SimpleGrantedAuthority> authorities =
            roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.name())).toList();

        AuthUser principal = new AuthUser(userId, username, roles);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (Exception ignored) {
        // token 无效就当未登录
        SecurityContextHolder.clearContext();
      }
    }

    filterChain.doFilter(request, response);
  }
}

