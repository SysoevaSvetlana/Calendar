package com.example.Calendar.repository;


import com.example.Calendar.model.Appointment;
import com.example.Calendar.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Найти записи в указанном временном диапазоне
    @Query("SELECT a FROM Appointment a WHERE a.startTime BETWEEN :start AND :end")
    List<Appointment> findBetweenDates(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    // Найти подтвержденные записи в диапазоне дат
    @Query("SELECT a FROM Appointment a WHERE a.status = 'CONFIRMED' AND a.startTime BETWEEN :start AND :end")
    List<Appointment> findConfirmedBetweenDates(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);

    // Найти запись по токену подтверждения
    Optional<Appointment> findByConfirmationToken(UUID confirmationToken);

    // Найти записи по статусу
    List<Appointment> findByStatus(AppointmentStatus status);

    // Проверить доступность временного слота
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Appointment a WHERE a.status = 'CONFIRMED' " +
            "AND ((a.startTime < :end AND a.endTime > :start))")
    boolean isSlotBooked(@Param("start") LocalDateTime start,
                         @Param("end") LocalDateTime end);

    // Обновить статус записи
    @Modifying
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") AppointmentStatus status);

    // Установить Google Event ID
    @Modifying
    @Query("UPDATE Appointment a SET a.googleEventId = :eventId WHERE a.id = :id")
    void setGoogleEventId(@Param("id") Long id, @Param("eventId") String eventId);
}