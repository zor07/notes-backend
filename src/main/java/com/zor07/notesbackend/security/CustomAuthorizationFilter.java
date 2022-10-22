package com.zor07.notesbackend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

public class CustomAuthorizationFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthorizationFilter.class);

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain) throws ServletException, IOException {
    if (request.getServletPath().equals("/api/v1/auth/login") || request.getServletPath().equals("/api/v1/auth/token/refresh")) {
      filterChain.doFilter(request, response);
    } else {
      final var authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
      if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        try {
          final var decodedJWT = SecurityUtils.decodeJWT(authorizationHeader);
          final var username = decodedJWT.getSubject();
          final var roles = decodedJWT.getClaim("roles").asArray(String.class);
          final var authorities = new ArrayList<SimpleGrantedAuthority>();
          Stream.of(roles)
              .forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
          final var authenticationToken =
              new UsernamePasswordAuthenticationToken(username, null, authorities);
          SecurityContextHolder.getContext().setAuthentication(authenticationToken);
          filterChain.doFilter(request, response);
        } catch (final Exception e) {
          LOGGER.error("Got exception while authorizing request", e);
          SecurityUtils.addErrorToResponse(response, e.getMessage());
        }
      } else {
        filterChain.doFilter(request, response);
      }
    }
  }


}
