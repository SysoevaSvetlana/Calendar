package com.example.Calendar.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "calendar_configs")
@Data
public class CalendarConfig {

    @Id
    @Column(name = "calendar_id")
    private String calendarId = "primary"; // ID календаря в Google

    @Column(name = "owner_email", nullable = false, unique = true)
    private String ownerEmail; // Email владельца календаря

    @Column(name = "time_zone", nullable = false)
    private String timeZone = "Europe/Moscow"; // Часовой пояс календаря

    @Column(name = "slot_duration_minutes", nullable = false)
    private int slotDurationMinutes = 60; // Длительность одного слота

    @Column(name = "working_hours_start", nullable = false)
    private int workingHoursStart =8; // Начало рабочего дня (часы)

    @Column(name = "working_hours_end", nullable = false)
    private int workingHoursEnd = 22; // Конец рабочего дня (часы)

    @Column(name = "advance_booking_days", nullable = false)
    private int advanceBookingDays = 60; // Максимальный срок бронирования
}