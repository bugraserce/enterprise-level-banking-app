package com.bank.identity;

import jakarta.validation.constraints.NotBlank;

// Login body: just the two keys. Password arrives plain over TLS and is
// checked with BCrypt inside AuthService, never stored or logged.
public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password) {
}
