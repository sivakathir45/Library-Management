package com.psgitech.library;

import com.psgitech.library.util.PasswordUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class PasswordUtilTest {

    @Test
    public void testBCryptHashingAndVerification() {
        String plain = "SecretPassword@2026";
        String hash = PasswordUtil.hashPassword(plain);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
        assertTrue(PasswordUtil.verifyPassword(plain, hash));
        assertFalse(PasswordUtil.verifyPassword("WrongPassword@2026", hash));
    }

    @Test
    public void testRememberMeTokenGenerationAndHashing() {
        String rawToken = PasswordUtil.generateRandomToken();
        assertNotNull(rawToken);
        assertTrue(rawToken.length() >= 32);

        String hash1 = PasswordUtil.hashToken(rawToken);
        String hash2 = PasswordUtil.hashToken(rawToken);

        assertNotNull(hash1);
        assertEquals(hash1, hash2); // Deterministic SHA-256 for token lookup
    }
}
