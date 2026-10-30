package com.psgitech.library.service;

import com.psgitech.library.exception.AccountInactiveException;
import com.psgitech.library.exception.InvalidCredentialsException;
import com.psgitech.library.model.User;
import com.psgitech.library.repository.UserRepository;
import com.psgitech.library.util.PasswordUtil;

import java.sql.Timestamp;

/**
 * Service handling user authentication, credential verification with BCrypt,
 * session validation, and Remember Me token lifecycle.
 */
public class AuthenticationService {
    public static final String REMEMBER_ME_COOKIE_NAME = "psgitech_remember_me";
    public static final int REMEMBER_ME_DAYS = 14;

    private final UserRepository userRepository;

    public AuthenticationService() {
        this.userRepository = new UserRepository();
    }

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Authenticates a user with email/identifier and plaintext password.
     */
    public User login(String loginKey, String plainPassword) throws InvalidCredentialsException, AccountInactiveException {
        if (loginKey == null || loginKey.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            throw new InvalidCredentialsException("Email/Register Number and Password are required.");
        }

        User user = userRepository.findByEmailOrIdentifier(loginKey.trim());
        if (user == null) {
            throw new InvalidCredentialsException("Invalid credentials. Please check your login details.");
        }

        if (!PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials. Please check your login details.");
        }

        if (!user.isActive()) {
            throw new AccountInactiveException("Your account is deactivated. Please contact the Library Administrator.");
        }

        return user;
    }

    /**
     * Creates a new Remember Me persistent token and saves its SHA-256 hash in MySQL.
     * Returns the raw random token to be placed in an HTTP-Only cookie.
     */
    public String createRememberToken(int userId) {
        String rawToken = PasswordUtil.generateRandomToken();
        String tokenHash = PasswordUtil.hashToken(rawToken);

        long expiryMillis = System.currentTimeMillis() + ((long) REMEMBER_ME_DAYS * 24 * 60 * 60 * 1000);
        Timestamp expiresAt = new Timestamp(expiryMillis);

        boolean saved = userRepository.saveRememberToken(userId, tokenHash, expiresAt);
        if (saved) {
            return rawToken;
        }
        return null;
    }

    /**
     * Validates a persistent token from the cookie and restores the User.
     */
    public User validateAndRestoreUserFromToken(String rawToken) {
        if (rawToken == null || rawToken.trim().isEmpty()) {
            return null;
        }

        String tokenHash = PasswordUtil.hashToken(rawToken.trim());
        Integer userId = userRepository.findValidUserIdByTokenHash(tokenHash);

        if (userId != null) {
            User user = userRepository.findById(userId);
            if (user != null && user.isActive()) {
                return user;
            }
        }
        return null;
    }

    /**
     * Revokes a persistent token upon logout.
     */
    public void revokeRememberToken(String rawToken) {
        if (rawToken != null && !rawToken.trim().isEmpty()) {
            String tokenHash = PasswordUtil.hashToken(rawToken.trim());
            userRepository.revokeRememberToken(tokenHash);
        }
    }

    /**
     * Revokes all active tokens for a user (e.g., after password change).
     */
    public void revokeAllUserTokens(int userId) {
        userRepository.revokeAllUserTokens(userId);
    }
}
