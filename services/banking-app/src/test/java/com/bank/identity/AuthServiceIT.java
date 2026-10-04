package com.bank.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

// @Testcontainers starts real Docker containers for tests.
// @SpringBootTest boots the whole app against them.
// @ServiceConnection wires the container as the app's datasource,
// so no hardcoded JDBC URL is needed in test config.
@Testcontainers
@SpringBootTest
@Transactional
class AuthServiceIT {

    // @Container + static = one Postgres for all tests in this class.
    // withDatabaseName must match nothing special; Flyway V1 builds the schema.
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    AuthService auth;

    @Autowired
    UserRepository users;

    // Happy path: valid data becomes a stored user with a hash, not the plain text.
    @Test
    void registersUserWithHashedPassword() {
        User saved = auth.register(new RegisterRequest("alice", "alice@bank.test", "secret123", "Alice Tester"));

        assertNotNull(saved.getId());
        assertEquals("alice", saved.getUsername());
        assertTrue(users.findByUsername("alice").isPresent());
    }

    // Guard path: the same username twice must fail loudly, never silently overwrite.
    @Test
    void rejectsDuplicateUsername() {
        auth.register(new RegisterRequest("bob", "bob@bank.test", "secret123", "Bob Tester"));

        assertThrows(IdentityTakenException.class, () ->
                auth.register(new RegisterRequest("bob", "other@bank.test", "secret123", "Bob Copy")));
    }

    // Login mints a usable pair: the access token carries this user's id.
    @Test
    void loginIssuesTokenPair(@Autowired JwtService jwt) {
        auth.register(new RegisterRequest("carol", "carol@bank.test", "secret123", "Carol Tester"));

        LoginResult pair = auth.login("carol", "secret123");

        assertNotNull(pair.accessToken());
        assertNotNull(pair.refreshToken());
        User user = users.findByUsername("carol").orElseThrow();
        assertEquals(user.getId().toString(), jwt.subjectOf(pair.accessToken()));
    }

    // Wrong password and unknown user look identical: one generic failure.
    @Test
    void loginRejectsBadCredentials() {
        auth.register(new RegisterRequest("dave", "dave@bank.test", "secret123", "Dave Tester"));

        assertThrows(InvalidCredentialsException.class, () -> auth.login("dave", "nope12345"));
        assertThrows(InvalidCredentialsException.class, () -> auth.login("ghost", "secret123"));
    }

    // Rotation: spending a refresh token kills it; replaying it fails.
    @Test
    void refreshRotatesSingleUse() {
        auth.register(new RegisterRequest("erin", "erin@bank.test", "secret123", "Erin Tester"));
        LoginResult first = auth.login("erin", "secret123");

        LoginResult second = auth.refresh(first.refreshToken());

        assertNotNull(second.accessToken());
        assertThrows(InvalidCredentialsException.class, () -> auth.refresh(first.refreshToken()));
    }
}