package com.bank.identity;

// A record is a small immutable carrier: two tokens, no behavior.
// accessToken = short-lived JWT (15 min), sent as Authorization header.
// refreshToken = opaque secret (7 days), shown once, stored only as a hash.
public record LoginResult(String accessToken, String refreshToken) {
}
