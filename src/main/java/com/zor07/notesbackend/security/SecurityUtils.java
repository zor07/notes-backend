package com.zor07.notesbackend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zor07.notesbackend.entity.Role;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SecurityUtils {

  private static Algorithm getAlgorithm() {
    return Algorithm.HMAC256("secret".getBytes());
  }

  private static String createAccessToken(final String username, final String issuer, final List<String> claims) {
    return JWT.create()
        .withSubject(username)
        .withExpiresAt(new Date(System.currentTimeMillis() + Duration.ofHours(1).toMillis()))
        .withIssuer(issuer)
        .withClaim("roles", claims)
        .sign(getAlgorithm());
  }

  public static boolean isUserAdmin(final com.zor07.notesbackend.entity.User user) {
    return user.getRoles().stream().anyMatch(role -> role.getName().equals(UserRole.ROLE_ADMIN.getRoleName()));
  }

  public static String createRefreshToken(final User user, final String issuer) {
    return JWT.create()
        .withSubject(user.getUsername())
        .withExpiresAt(new Date(System.currentTimeMillis() + Duration.ofDays(10).toMillis()))
        .withIssuer(issuer)
        .sign(getAlgorithm());
  }

  public static String createAccessToken(final com.zor07.notesbackend.entity.User user, final String issuer) {
    return createAccessToken(user.getUsername(), issuer,
        user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
  }

  public static String createAccessToken(final User user, final String issuer) {
    return createAccessToken(user.getUsername(), issuer,
        user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
  }

  public static String parseRefreshToken(final String authorizationHeader) {
    return authorizationHeader.substring("Bearer ".length());
  }

  public static void addErrorToResponse(final HttpServletResponse response,
                                        final String errorMessage) throws IOException {
    final var error = new HashMap<String, String>();
    error.put("error_message", errorMessage);
    response.setHeader("error", errorMessage);
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    new ObjectMapper().writeValue(response.getOutputStream(), error);
  }

  public static DecodedJWT decodeJWT(final String authorizationHeader) {
    final var token = parseRefreshToken(authorizationHeader);
    final var algorithm = getAlgorithm();
    final var verifier = JWT.require(algorithm).build();
    return verifier.verify(token);
  }

}
