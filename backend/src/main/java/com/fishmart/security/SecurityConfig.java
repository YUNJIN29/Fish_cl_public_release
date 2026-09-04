package com.fishmart.security;

import com.fishmart.config.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  private final JwtAuthFilter jwtAuthFilter;
  private final AppProperties props;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter, AppProperties props) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.props = props;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .requestMatchers("/uploads/**").permitAll()
            .requestMatchers("/print-files/**").permitAll()
            .requestMatchers("/diagnose-images/**").permitAll()
            .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
            .requestMatchers("/api/print/**").permitAll()
            .requestMatchers("/api/visits/**").permitAll()
            .requestMatchers("/api/game-scores/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/articles/**", "/api/posts/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/comments").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/diseases/**", "/api/knowledge-graph").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/rag/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/diagnose").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(props.getCors().getAllowedOrigins());
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
