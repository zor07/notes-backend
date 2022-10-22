package com.zor07.notesbackend.api.v1;

import com.zor07.notesbackend.api.v1.dto.auth.TokensDto;
import com.zor07.notesbackend.repository.RoleRepository;
import com.zor07.notesbackend.repository.UserRepository;
import com.zor07.notesbackend.spring.AbstractApiTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AuthControllerTest extends AbstractApiTest {

  private static final String LOGIN_PAYLOAD = """
        {
          "username": "user",
          "password": "pass"
        }
        """;

  @Autowired
  private WebApplicationContext context;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private RoleRepository roleRepository;

  private MockMvc mvc;

  private void clearDb () {
    userRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @BeforeClass
  public void setup() {
    clearDb();
    userService.saveUser(createUser(DEFAULT_USERNAME));
    userService.saveRole(createRole());
    userService.addRoleToUser(DEFAULT_USERNAME, DEFAULT_ROLE);
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @AfterClass
  void teardown() {
    clearDb();
  }

  @Test
  void login_isOk_test() throws Exception {
    mvc.perform(post(LOGIN_ENDPOINT).servletPath(LOGIN_ENDPOINT).content(LOGIN_PAYLOAD).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void login_isUnauthorized_test() throws Exception {
    final var payload = """
        {
          "username": "wrong_user",
          "password": "pass"
        }
        """;

    mvc.perform(post(LOGIN_ENDPOINT).servletPath(LOGIN_ENDPOINT).content(payload).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refreshToken_isOK_test() throws Exception {
    mvc.perform(post(LOGIN_ENDPOINT).servletPath(LOGIN_ENDPOINT).content(LOGIN_PAYLOAD).contentType(MediaType.APPLICATION_JSON))
        .andDo(loginResult -> {
          final var tokens = objectMapper.readValue(loginResult.getResponse().getContentAsString(), TokensDto.class);
          final var authHeader = String.format("Bearer %s", tokens.refreshToken());
          final var refreshTokenResult =
              mvc.perform(get(REFRESH_TOKEN_ENDPOINT)
                  .servletPath(REFRESH_TOKEN_ENDPOINT)
                  .header(HttpHeaders.AUTHORIZATION, authHeader)
                  .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

          final var newTokens = objectMapper.readValue(refreshTokenResult.getResponse().getContentAsString(), TokensDto.class);
          assertThat(newTokens.refreshToken()).isEqualTo(tokens.refreshToken());
          assertThat(newTokens.accessToken()).isNotEqualTo(tokens.accessToken());
        });
  }

}
