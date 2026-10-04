package com.bank.identity;

// Thrown when login or refresh fails for ANY reason: unknown user,
// locked account, wrong password, dead token. One type means the HTTP
// layer always answers 401 with one identical shape (no user probing).
public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException() {
    super("invalid credentials");
  }
}
