package com.zor07.notesbackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zor07.notesbackend.api.v1.dto.auth.AuthenticationDto;
import com.zor07.notesbackend.api.v1.dto.auth.TokensDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationFilter.class);

  private final AuthenticationManager authenticationManager;
  private final ObjectMapper objectMapper;

  public CustomAuthenticationFilter(final AuthenticationManager authenticationManager,
      final ObjectMapper objectMapper) {
    this.authenticationManager = authenticationManager;
    this.objectMapper = objectMapper;
  }

  @Override
  public Authentication attemptAuthentication(final HttpServletRequest request,
      final HttpServletResponse response) throws AuthenticationException {
    final var authData = getAuthDataFromRequest(request);
    final var authenticationToken = new UsernamePasswordAuthenticationToken(authData.username(), authData.password());
    return authenticationManager.authenticate(authenticationToken);
  }

  @Override
  protected void successfulAuthentication(final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain chain,
      final Authentication authentication) throws IOException {
    final var user = (User) authentication.getPrincipal();
    final var accessToken = SecurityUtils.createAccessToken(user, request.getRequestURL().toString());
    final var refreshToken = SecurityUtils.createRefreshToken(user, request.getRequestURL().toString());
    final var tokens = new TokensDto(accessToken, refreshToken);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    new ObjectMapper().writeValue(response.getOutputStream(), tokens);
  }

  private AuthenticationDto getAuthDataFromRequest(final HttpServletRequest request) {
    try {
      final var body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
      return objectMapper.readValue(body, AuthenticationDto.class);
    } catch (final IOException e) {
      LOGGER.error("Cannot get body from request", e);
      return new AuthenticationDto(null, null);
    }
  }
}
