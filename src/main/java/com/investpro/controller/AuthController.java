package com.investpro.controller;

import com.investpro.model.User;
import com.investpro.service.AuthService;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String SESSION_USER_ID = "INVESTPRO_USER_ID";
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request, HttpSession session) {
        User user = auth.register(request.fullName(), request.email(), request.password());
        session.setAttribute(SESSION_USER_ID, user.getId());
        return Map.of("user", user, "message", "Account created successfully.");
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request, HttpSession session) {
        User user = auth.login(request.email(), request.password());
        session.setAttribute(SESSION_USER_ID, user.getId());
        return Map.of("user", user, "message", "Welcome back.");
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "You have been signed out.");
    }

    @GetMapping("/me")
    public User me(HttpSession session) { return currentUser(session); }

    public User currentUser(HttpSession session) {
        Object id = session.getAttribute(SESSION_USER_ID);
        if (!(id instanceof Number number)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in to continue.");
        }
        return auth.findById(number.longValue());
    }

    public long currentUserId(HttpSession session) { return currentUser(session).getId(); }

    public record RegisterRequest(String fullName, String email, String password) { }
    public record LoginRequest(String email, String password) { }
}
