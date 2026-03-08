package com.xietg.kc.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    void should_hash_and_verify_password() {
        String rawPassword = "S3cretPwd!";
        String hash = passwordService.hashPassword(rawPassword);

        assertNotNull(hash);
        assertNotEquals(rawPassword, hash);
        assertTrue(passwordService.verifyPassword(rawPassword, hash));
    }

    @Test
    void should_reject_wrong_password() {
        String hash = passwordService.hashPassword("good-password");

        assertFalse(passwordService.verifyPassword("bad-password", hash));
    }

    @Test
    void should_generate_different_hashes_for_same_password() {
        String rawPassword = "same-password";

        String hash1 = passwordService.hashPassword(rawPassword);
        String hash2 = passwordService.hashPassword(rawPassword);

        assertNotEquals(hash1, hash2);
        assertTrue(passwordService.verifyPassword(rawPassword, hash1));
        assertTrue(passwordService.verifyPassword(rawPassword, hash2));
    }
}