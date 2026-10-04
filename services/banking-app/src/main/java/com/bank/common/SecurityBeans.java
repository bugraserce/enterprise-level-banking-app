package com.bank.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// @Configuration marks this class as a source of shared objects.
// Spring reads it once at startup and keeps what it produces.
@Configuration
public class SecurityBeans {

    // @Bean tells Spring to build this object once and hand it to
    // whoever asks (here: AuthService for hashing passwords).
    // BCrypt is a one-way hash: a password goes in, gibberish comes out,
    // and nobody can reverse it. Strength 12 means each hash takes about
    // a quarter second — slow enough to hurt attackers guessing millions
    // of passwords, fast enough that a real login feels instant.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}