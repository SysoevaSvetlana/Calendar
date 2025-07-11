package com.example.Calendar.controller;
import com.example.Calendar.model.User;
import com.example.Calendar.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Генерирует URL авторизации Google и редиректит пользователя
     */
    @GetMapping("/oauth2/authorize/google")
    public void redirectToGoogleOAuth(HttpServletResponse response) throws IOException {
        String redirectUri = "http://localhost:8080/api/auth/oauth2/callback";
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

    @GetMapping("/oauth2/callback")
    public void handleOAuthCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        User user = null;
        try {
            user = authService.authenticateWithGoogle(code);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        ObjectMapper mapper = new ObjectMapper();
        String userJson = mapper.writeValueAsString(user);
        String escapedUserJson = userJson.replace("'", "\\'");

        String script = "<script>" +
                "window.opener.postMessage({ type: 'oauth-success', user: " + userJson + " }, '*');" +
                "window.close();" +
                "</script>";


        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(script);
    }
    /**
     * Обновление accessToken вручную
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestParam String userId) throws IOException {
        String newToken = authService.refreshAccessToken(userId);
        return ResponseEntity.ok(Collections.singletonMap("accessToken", newToken));
    }
    public record AuthCodeRequest(String code) {}

}
