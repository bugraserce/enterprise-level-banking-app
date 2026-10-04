package com.bank.identity;

import java.time.OffsetDateTime;
import java.util.UUID;
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
    private final PasswordEncoder encoder;

    // Constructor injection: Spring supplies the repository and the
    // hasher when it builds this service. Storing them in final fields
    // makes the dependency visible and the class safe to share.
    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
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
}