package com.bank.identity;

// Outbound DTO: the token pair plus its scheme. Entities never leave the
// service layer, so password hashes can never leak through JSON.
public record AuthResponse(String accessToken, String refreshToken, String tokenType) {

  public AuthResponse(String accessToken, String refreshToken) {
    this(accessToken, refreshToken, "Bearer");
  }
}
