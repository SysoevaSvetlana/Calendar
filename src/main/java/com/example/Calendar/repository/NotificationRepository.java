package com.example.Calendar.repository;


import com.example.Calendar.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Найти непрочитанные уведомления для email
    List<Notification> findByRecipientEmailAndIsReadFalse(String email);

    // Найти уведомления по связанной записи
    List<Notification> findByRelatedAppointmentId(Long appointmentId);

    // Пометить уведомления как прочитанные
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id IN :ids")
    void markAsRead(@Param("ids") List<Long> ids);
}