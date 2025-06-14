package com.example.Calendar.controller;


import com.example.Calendar.model.User;
import com.example.Calendar.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<User> authenticateWithGoogle(@RequestParam String code) throws IOException, GeneralSecurityException {
        return ResponseEntity.ok(authService.authenticateWithGoogle(code));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestParam String userId) throws IOException {
        String newToken = authService.refreshAccessToken(userId);
        return ResponseEntity.ok(Collections.singletonMap("accessToken", newToken));
    }
    @GetMapping("/success")
    public ResponseEntity<String> success(Authentication authentication) {
        return ResponseEntity.ok("Успешный вход! Пользователь: " + authentication.getName());
    }
    @GetMapping("/oauth2/authorize/google")
    public void redirectToGoogleOAuth(HttpServletResponse response) throws IOException {
        String redirectUri = "http://localhost:8080/api/oauth2/callback"; // замени на актуальный
        String clientId = "1091225339493-ovndajhj9m5ipsakgoclpnoehnvl4sps.apps.googleusercontent.com";
        String scope = "https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/userinfo.email openid";
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8)
                + "&access_type=offline"
                + "&prompt=consent";

        response.sendRedirect(authUrl);
    }

}