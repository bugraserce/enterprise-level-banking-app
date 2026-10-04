package com.bank.identity;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// @Service marks this class as a stateless crypto helper so Spring finds
// it automatically. Secrets come from env config, never from code.
@Service
public class JwtService {

  private final SecretKey key;
  private final Duration accessTtl;

  // @Value reads config with a local default after the colon.
  // banking.security.jwt.secret MUST be >= 32 chars (256 bits for HS256).
  public JwtService(
      @Value("${banking.security.jwt.secret:local-dev-secret-local-dev-secret-12}") String secret,
      @Value("${banking.security.jwt.access-ttl:PT15M}") Duration accessTtl) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTtl = accessTtl;
  }

  // Short-lived access token: who you are plus what you may do, signed.
  // Fifteen minutes bounds the damage if one leaks.
  public String accessToken(User user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(user.getId().toString())
        .claim("username", user.getUsername())
        .claim("roles", "ROLE_USER")
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(accessTtl)))
        .signWith(key)
        .compact();
  }

  // Reads and trusts a token's signature plus expiry. Throws on tamper or
  // expiry. Filters lean on this: no DB call per request (stateless win).
  public String subjectOf(String token) {
    return Jwts.parser().verifyWith(key).build()
        .parseSignedClaims(token).getPayload().getSubject();
  }

  // Opaque refresh token: 256 random bits, shown once. Returns the PLAIN
  // value; only its SHA-256 fingerprint goes to the database (AuthService).
  public String newRefreshToken() {
    byte[] random = new byte[32];
    new SecureRandom().nextBytes(random);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
  }

  // SHA-256 fingerprint used as the DB lookup key. Match means same token.
  public String sha256(String plain) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
          .digest(plain.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
