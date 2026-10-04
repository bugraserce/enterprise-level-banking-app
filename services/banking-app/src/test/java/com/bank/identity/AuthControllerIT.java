package com.bank.identity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

// HTTP proof: real routes, real filter chain, real Postgres. @Transactional
// rolls each test back, so users never leak between cases.
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIT {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired
  MockMvc mvc;

  @Autowired
  ObjectMapper json;

  private String body(Map<String, String> fields) throws Exception {
    return json.writeValueAsString(fields);
  }

  // 201 + token pair on first signup.
  @Test
  void registerCreatesUserAndTokens() throws Exception {
    mvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("username", "fred", "email", "fred@bank.test",
                "password", "secret123", "fullName", "Fred Tester"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.refreshToken").isNotEmpty())
        .andExpect(jsonPath("$.tokenType").value("Bearer"));
  }

  // Second signup with the same name is 409, not a silent overwrite.
  @Test
  void registerConflictsOnDuplicate() throws Exception {
    String same = body(Map.of("username", "gina", "email", "gina@bank.test",
        "password", "secret123", "fullName", "Gina Tester"));
    mvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON).content(same)).andExpect(status().isCreated());

    mvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON).content(same))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").exists());
  }

  // Login returns a pair; bad password returns the same 401 as ghost users.
  @Test
  void loginSucceedsAndBadPasswordIs401() throws Exception {
    mvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body(Map.of("username", "hank", "email", "hank@bank.test",
            "password", "secret123", "fullName", "Hank Tester")))).andExpect(status().isCreated());

    mvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("username", "hank", "password", "secret123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty());

    mvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("username", "hank", "password", "wrongpass1"))))
        .andExpect(status().isUnauthorized());
  }

  // A valid JWT opens locked doors; no token means 403/401 on them.
  @Test
  void jwtOpensProtectedEndpoints() throws Exception {
    String signup = mvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("username", "iris", "email", "iris@bank.test",
                "password", "secret123", "fullName", "Iris Tester"))))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    String token = json.readTree(signup).get("accessToken").asText();

    // No such route yet, so an authenticated call lands on 404 —
    // which itself proves the filter let it THROUGH (else 403).
    mvc.perform(get("/api/v1/accounts").header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/accounts"))
        .andExpect(status().isForbidden());
  }

  // Too-short passwords never reach the service: 400 with field detail.
  @Test
  void shortPasswordIs400() throws Exception {
    mvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("username", "joe", "email", "joe@bank.test",
                "password", "short", "fullName", "Joe Tester"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fields.password").exists());
  }
}
