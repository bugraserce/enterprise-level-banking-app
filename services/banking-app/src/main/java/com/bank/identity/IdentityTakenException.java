package com.bank.identity;

// Thrown when a username or email is already taken. Maps to 409 CONFLICT:
// the request itself is fine, it collides with existing state.
public class IdentityTakenException extends RuntimeException {

  public IdentityTakenException(String what) {
    super(what + " taken");
  }
}
