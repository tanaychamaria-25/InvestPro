package com.investpro.service;

import com.investpro.model.User;
import com.investpro.repository.UserRepository;
import com.investpro.security.PasswordUtil;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UserRepository users;

    public AuthService(UserRepository users) { this.users = users; }

    public User register(String fullName, String email, String password) {
        String cleanName = fullName == null ? "" : fullName.trim();
        String cleanEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (cleanName.length() < 2 || cleanName.length() > 100) {
            throw new IllegalArgumentException("Enter a name between 2 and 100 characters.");
        }
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches() || cleanEmail.length() > 190) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (password == null || password.length() < 6 || password.length() > 100) {
            throw new IllegalArgumentException("Password must be between 6 and 100 characters.");
        }
        if (users.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("An account with that email already exists. Please sign in.");
        }
        try {
            users.create(cleanName, cleanEmail, PasswordUtil.hash(password));
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("An account with that email already exists. Please sign in.");
        }
        return users.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalStateException("The account was created but could not be loaded."));
    }

    public User login(String email, String password) {
        String cleanEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String storedHash = users.findPasswordHashByEmail(cleanEmail).orElse(null);
        if (storedHash == null || password == null || !PasswordUtil.verify(password, storedHash)) {
            throw new IllegalArgumentException("Email or password is incorrect.");
        }
        return users.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Email or password is incorrect."));
    }

    public User findById(long id) {
        return users.findById(id).orElseThrow(() -> new IllegalArgumentException("Your account could not be found."));
    }
}
