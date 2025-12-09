package com.zor07.notesbackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class SecurityConfig {

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder bCryptPasswordEncoder;
  private final ObjectMapper objectMapper;

  @Autowired
  public SecurityConfig(final UserDetailsService userDetailsService,
      final PasswordEncoder passwordEncoder,
      final ObjectMapper objectMapper) {
    this.userDetailsService = userDetailsService;
    this.bCryptPasswordEncoder = passwordEncoder;
    this.objectMapper = objectMapper;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    final var authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    final var authenticationManager = authenticationManagerBuilder.build();

    final var filter = new CustomAuthenticationFilter(authenticationManager, objectMapper);
    filter.setFilterProcessesUrl("/api/v1/auth/login");
    http
            .authorizeHttpRequests((authz) -> authz
                    .antMatchers("/api/v1/auth/login/**",
                            "/spring-security-rest/**",
                            "/swagger-ui/**",
                            "/api/v1/auth/token/refresh/**",
                            "/api/v1/auth/register/**").permitAll()
                    .antMatchers("/api/**").hasAnyAuthority(UserRole.ROLE_USER.getRoleName(), UserRole.ROLE_ADMIN.getRoleName())
                    .anyRequest().authenticated()
                    .and().authenticationManager(authenticationManager)
            )
            .cors().and().csrf().disable()
            .addFilter(filter)
            .addFilterBefore(new CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web
            .ignoring()
            .antMatchers("/v2/api-docs",
                    "/configuration/ui",
                    "/swagger-resources/**",
                    "/configuration/security",
                    "/swagger-ui.html",
                    "/webjars/**");
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final var configuration = new CorsConfiguration();
    configuration.setAllowCredentials(true);
    configuration.setAllowedHeaders(Collections.singletonList("*"));
    configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
    configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    final var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH", "OPTIONS");
      }
    };
  }
}
