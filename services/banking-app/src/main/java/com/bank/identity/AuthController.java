package com.bank.identity;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// @RestController = the HTTP door of the identity domain. Thin by rule:
// validate the body (@Valid), call AuthService, wrap the answer. No SQL,
// no hashing, no token math in this class — those live in the service.
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService auth;

  public AuthController(AuthService auth) {
    this.auth = auth;
  }

  // 201: a new identity was created. Returns tokens immediately so the
  // client can proceed without a second login round-trip.
  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
    auth.register(req);
    LoginResult pair = auth.login(req.username(), req.password());
    return new AuthResponse(pair.accessToken(), pair.refreshToken());
  }

  // 200 + token pair, or 401 with the generic shape (see handler).
  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest req) {
    LoginResult pair = auth.login(req.username(), req.password());
    return new AuthResponse(pair.accessToken(), pair.refreshToken());
  }

  // Single-use rotation: old refresh dies, fresh pair is born.
  @PostMapping("/refresh")
  public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
    LoginResult pair = auth.refresh(req.refreshToken());
    return new AuthResponse(pair.accessToken(), pair.refreshToken());
  }
}
