package com.example.Calendar.repository;


import com.example.Calendar.model.CalendarConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CalendarConfigRepository extends JpaRepository<CalendarConfig, String> {

    // Найти конфигурацию по ID календаря
    Optional<CalendarConfig> findByCalendarId(String calendarId);

    // Найти конфигурацию по email владельца
    Optional<CalendarConfig> findByOwnerEmail(String ownerEmail);

    // Обновить рабочие часы
    @Modifying
    @Query("UPDATE CalendarConfig c SET c.workingHoursStart = :start, " +
            "c.workingHoursEnd = :end WHERE c.calendarId = :calendarId")
    void updateWorkingHours(@Param("calendarId") String calendarId,
                            @Param("start") int startHour,
                            @Param("end") int endHour);
}
