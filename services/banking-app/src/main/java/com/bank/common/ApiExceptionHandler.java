package com.bank.common;

import com.bank.identity.IdentityTakenException;
import com.bank.identity.InvalidCredentialsException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// @RestControllerAdvice = one translator for the whole API. Service
// exceptions become HTTP statuses with small JSON bodies. Stack traces
// never reach the client (they would leak internals to attackers).
@RestControllerAdvice
public class ApiExceptionHandler {

  // 401 with one identical shape for every login failure.
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Map<String, String>> invalid(InvalidCredentialsException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
  }

  // 409: the request is valid, it collides with existing state.
  @ExceptionHandler(IdentityTakenException.class)
  public ResponseEntity<Map<String, String>> taken(IdentityTakenException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
  }

  // 400: @Valid rejected the body. Returns WHICH fields and why, so the
  // frontend can highlight them instead of guessing.
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> invalidBody(MethodArgumentNotValidException e) {
    Map<String, String> fields = new HashMap<>();
    e.getBindingResult().getFieldErrors()
        .forEach(f -> fields.put(f.getField(), f.getDefaultMessage()));
    Map<String, Object> body = new HashMap<>();
    body.put("error", "validation failed");
    body.put("fields", fields);
    return ResponseEntity.badRequest().body(body);
  }
}
