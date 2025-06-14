package com.example.Calendar.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "oauth_tokens")
@Data
public class OAuthToken {

    @Id
    private String userId; // Связь с пользователем

    @Column(nullable = false)
    private String accessToken;

    @Column
    private String refreshToken;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate; // Срок действия токена

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt; // Время выдачи токена
    @Column
    private String scope;
}