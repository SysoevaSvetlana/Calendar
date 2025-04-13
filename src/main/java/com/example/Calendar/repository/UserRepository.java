package com.example.Calendar.repository;


import com.example.Calendar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Найти пользователя по email
    Optional<User> findByEmail(String email);

    // Найти пользователя по Google ID
    Optional<User> findByGoogleId(String googleId);

    // Проверить существование пользователя по email
    boolean existsByEmail(String email);

    // Найти владельца календаря (администратора)
    @Query("SELECT u FROM User u WHERE u.isCalendarOwner = true")
    Optional<User> findCalendarOwner();

    // Обновить Google ID пользователя
    @Modifying
    @Query("UPDATE User u SET u.googleId = :googleId WHERE u.id = :userId")
    void updateGoogleId(@Param("userId") Long userId, @Param("googleId") String googleId);
}