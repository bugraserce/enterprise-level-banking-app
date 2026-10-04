package com.bank.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// A record is a small immutable carrier for the four signup fields.
// @NotBlank rejects empty or whitespace-only text before anything else.
// @Email accepts only text shaped like an address (checked by pattern,
// not by sending mail). @Size caps lengths so oversized input is refused
// at the door instead of failing deep inside the database.
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 255) String fullName) {
}