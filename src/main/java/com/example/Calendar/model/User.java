package com.example.Calendar.model;



import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(name = "google_id", unique = true)
    private String googleId; // Уникальный идентификатор из Google OAuth

    @Column(name = "is_calendar_owner", nullable = false)
    private boolean isCalendarOwner = false; // Флаг владельца календаря

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Appointment> appointments = new HashSet<>();



    // Методы для работы с OAuth
    public void setGoogleCredentials(String googleId) {
        this.googleId = googleId;
    }

    public boolean isCalendarOwner() {
        return isCalendarOwner;
    }
}