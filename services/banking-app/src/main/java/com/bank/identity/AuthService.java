package com.bank.identity;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

// @Service marks this class as business logic so Spring finds it
// automatically and lets other classes receive it by injection.
// @Transactional wraps a method in one database transaction: every
// change inside either commits together or rolls back together,
// so a crash can never leave a half-written user behind.
@Service
public class AuthService {

  private final UserRepository users;
  private final RefreshTokenRepository refreshTokens;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final Duration refreshTtl;

  // Constructor injection: Spring supplies the repository, the refresh
  // store, the hasher and the token mint. refreshTtl reads config and
  // falls back to 7 days locally. Final fields keep it share-safe.
  public AuthService(UserRepository users, RefreshTokenRepository refreshTokens, PasswordEncoder encoder, JwtService jwt, @Value("${banking.security.refresh-ttl:P7D}") Duration refreshTtl) {
    this.users = users;
    this.refreshTokens = refreshTokens;
    this.encoder = encoder;
    this.jwt = jwt;
    this.refreshTtl = refreshTtl;
  }

  // @Transactional on register means the two "taken?" checks and the
  // insert below succeed or fail as a single unit. The UNIQUE columns
  // in auth.users are the final guard: if two people grab the same name
  // at the same instant, the database rejects the second commit.
  @Transactional
  public User register(RegisterRequest req) {
    // Login keys are normalized so "Alice", "alice" and " alice " all
    // point to the same account instead of three lookalikes.
    String username = req.username().trim().toLowerCase();
    String email = req.email().trim().toLowerCase();
    // Fast existence checks give a clear error early. They race by
    // nature, which is why the UNIQUE constraint still has the last word.
    if (users.existsByUsername(username)) {
      throw new IllegalArgumentException("username taken");
    }
    if (users.existsByEmail(email)) {
      throw new IllegalArgumentException("email taken");
    }
    // encode() turns the plain password into a BCrypt hash. The plain
    // text is never stored, never logged, and forgotten right here.
    // UUID.randomUUID() gives an opaque id that reveals nothing about
    // how many users exist, unlike counting 1, 2, 3...
    User user = new User(
        UUID.randomUUID(),
        username,
        email,
        encoder.encode(req.password()),
        req.fullName().trim(),
        true,
        OffsetDateTime.now(),
        OffsetDateTime.now());
    // save() inserts the row into auth.users within this transaction.
    return users.save(user);
  }

  // Login: same error for "no such user", "locked", and "wrong password".
  // One identical shape means attackers learn nothing by guessing names.
  // Success mints a 15-minute access JWT plus a one-time 7-day refresh
  // token whose SHA-256 fingerprint is stored for later rotation.
  @Transactional
  public LoginResult login(String username, String password) {
    String key = username.trim().toLowerCase();
    User user = users.findByUsername(key)
        .filter(User::isEnabled)
        .filter(u -> encoder.matches(password, u.getPasswordHash()))
        .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));
    return issuePair(user);
  }

  // Rotation: the presented refresh token dies here (revoked = true) and
  // a fresh pair is born. A stolen token is therefore single-use: the
  // first spend (thief or owner) kills it, the second spend fails loudly.
  // Expired or unknown tokens get the same generic error as bad logins.
  @Transactional
  public LoginResult refresh(String refreshToken) {
    RefreshToken row = refreshTokens.findByTokenHash(jwt.sha256(refreshToken))
        .filter(t -> !t.isRevoked())
        .filter(t -> t.getExpiresAt().isAfter(OffsetDateTime.now()))
        .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));
    row.setRevoked(true);
    User user = users.findById(row.getUserId())
        .filter(User::isEnabled)
        .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));
    return issuePair(user);
  }

  // Single private helper behind both entries: build the access JWT, mint
  // a fresh opaque refresh, store only its hash with an expiry, and hand
  // both plain values to the caller exactly once.
  private LoginResult issuePair(User user) {
    String refresh = jwt.newRefreshToken();
    refreshTokens.save(new RefreshToken(
        UUID.randomUUID(),
        user.getId(),
        jwt.sha256(refresh),
        OffsetDateTime.now().plus(refreshTtl),
        false,
        OffsetDateTime.now()));
    return new LoginResult(jwt.accessToken(user), refresh);
  }
}
