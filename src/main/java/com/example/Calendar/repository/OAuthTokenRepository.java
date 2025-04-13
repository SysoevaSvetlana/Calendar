package com.example.Calendar.repository;


import com.example.Calendar.model.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, String> {

    // Найти токен по ID пользователя
    Optional<OAuthToken> findByUserId(String userId);

    // Обновить токен доступа
    @Modifying
    @Query("UPDATE OAuthToken t SET t.accessToken = :token, " +
            "t.expiryDate = :expiry WHERE t.userId = :userId")
    void updateAccessToken(@Param("userId") String userId,
                           @Param("token") String token,
                           @Param("expiry") Instant expiry);

    // Удалить просроченные токены
    @Modifying
    @Query("DELETE FROM OAuthToken t WHERE t.expiryDate < :now")
    void cleanupExpiredTokens(@Param("now") Instant now);
}