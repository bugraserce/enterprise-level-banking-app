package com.bank.common;

import com.bank.identity.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @EnableWebSecurity switches on the filter chain. This class is the HTTP
// rulebook: which doors are open, which need a valid JWT. Stateless: no
// sessions, so no session fixation, and CSRF is disabled (CSRF only
// threatens cookie-based logins; our tokens travel in headers).
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthFilter jwtFilter;

  public SecurityConfig(JwtAuthFilter jwtFilter) {
    this.jwtFilter = jwtFilter;
  }

  @Bean
  public SecurityFilterChain chain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Open doors: signup/login/refresh plus the health probe.
            .requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll()
            // Everything else (accounts, transfers tomorrow) needs JWT.
            .anyRequest().authenticated())
        // Our checker runs before the username/password form filter.
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
