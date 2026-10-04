package com.bank.identity;

import jakarta.validation.constraints.NotBlank;

// Refresh body: the opaque token from login. Single-use by design:
// presenting it kills it and mints a fresh pair (rotation).
public record RefreshRequest(
    @NotBlank String refreshToken) {
}
