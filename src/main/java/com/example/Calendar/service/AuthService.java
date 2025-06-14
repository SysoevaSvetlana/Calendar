package com.example.Calendar.service;



import com.example.Calendar.model.OAuthToken;
import com.example.Calendar.model.User;
import com.example.Calendar.repository.OAuthTokenRepository;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.client.redirect-uri}")
    private String redirectUri;

    private final UserService userService;
    private final OAuthTokenRepository tokenRepository;

    public User authenticateWithGoogle(String authCode) throws IOException, GeneralSecurityException {
        // Обмен кода на токены
        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                new GsonFactory(),
                clientId,
                clientSecret,
                authCode,
                redirectUri)
                .execute();

        // Верификация ID токена
        GoogleIdToken idToken = verifyIdToken(tokenResponse.getIdToken());
        GoogleIdToken.Payload payload = idToken.getPayload();

        // Сохранение токенов
        OAuthToken token = new OAuthToken();
        token.setUserId(payload.getSubject());
        token.setAccessToken(tokenResponse.getAccessToken());
        token.setRefreshToken(tokenResponse.getRefreshToken());
        token.setExpiryDate(Instant.now().plusSeconds(tokenResponse.getExpiresInSeconds()));
        token.setIssuedAt(Instant.now());
        token.setScope(tokenResponse.getScope());
        tokenRepository.save(token);

        return userService.findOrCreateUser(idToken);
    }

    public String refreshAccessToken(String userId) throws IOException {
        OAuthToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        if (token.getRefreshToken() == null) {
            throw new RuntimeException("No refresh token available");
        }

        GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(
                new NetHttpTransport(),
                new GsonFactory(),
                token.getRefreshToken(),
                clientId,
                clientSecret)
                .execute();

        token.setAccessToken(tokenResponse.getAccessToken());
        token.setExpiryDate(Instant.now().plusSeconds(tokenResponse.getExpiresInSeconds()));
        tokenRepository.save(token);

        return tokenResponse.getAccessToken();
    }

    private GoogleIdToken verifyIdToken(String idTokenString) throws IOException, GeneralSecurityException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new RuntimeException("Invalid ID token");
        }
        return idToken;
    }
}