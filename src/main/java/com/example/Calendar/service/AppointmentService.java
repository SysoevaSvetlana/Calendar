package com.example.Calendar.service;


import com.example.Calendar.dto.BusySlotDto;
import com.example.Calendar.model.Appointment;
import com.example.Calendar.model.AppointmentStatus;
import com.example.Calendar.model.User;
import com.example.Calendar.repository.AppointmentRepository;
import com.example.Calendar.repository.CalendarConfigRepository;
import com.example.Calendar.repository.UserRepository;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final CalendarConfigRepository calendarConfigRepository;
    private final EmailService emailService;
    private final GoogleCalendarService googleCalendarService;




    private final ZoneId zone = ZoneId.of("Europe/Moscow");

    @Transactional(readOnly = true)
    public List<BusySlotDto> getBusySlots(LocalDateTime from, LocalDateTime to)
            throws IOException {

        List<BusySlotDto> result = new ArrayList<>();

        for (Event ev : googleCalendarService.getEvents(from, to)) {
            result.add(toDto(ev));
        }
        return List.copyOf(result);
    }



    private BusySlotDto toDto(Event ev) {
        OffsetDateTime start = toOffset(ev.getStart());
        OffsetDateTime end   = toOffset(ev.getEnd());

        return new BusySlotDto(
                start.toString(),
                end.toString(),
                "Занято",
                "#d3d3d3",
                "background"

        );
    }

    private OffsetDateTime toOffset(EventDateTime edt) {
        // либо dateTime (точное), либо date (all-day)
        if (edt.getDateTime() != null) {
            Instant instant = Instant.ofEpochMilli(edt.getDateTime().getValue());
            return instant.atZone(zone).toOffsetDateTime();
        }
        LocalDate ld = LocalDate.parse(edt.getDate().toStringRfc3339());
        return ld.atStartOfDay(zone).toOffsetDateTime();
    }



    @Transactional
    public Appointment createAppointment(Appointment appointment, String ownerEmail) {
        // Проверка доступности слота
        if (appointmentRepository.isSlotBooked(appointment.getStartTime(), appointment.getEndTime())) {
            throw new IllegalStateException("This time slot is already booked");
        }

        // Установка владельца календаря
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Calendar owner not found"));
        appointment.setUser(owner);

        // Генерация токена подтверждения
        appointment.generateConfirmationToken();
        appointment.setStatus(AppointmentStatus.PENDING);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Отправка уведомления владельцу
        emailService.sendConfirmationRequest(ownerEmail, savedAppointment);

        return savedAppointment;
    }

    @Transactional
    public void confirmAppointment(UUID confirmationToken) throws IOException {
        Appointment appointment = appointmentRepository.findByConfirmationToken(confirmationToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid confirmation token"));

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Appointment already processed");

        }

        if (!googleCalendarService.isSlotAvailable(appointment.getStartTime(), appointment.getEndTime())) {
            throw new IllegalStateException("This slot is already taken (Google Calendar)");
        }

        //что по срокам действия???????

        // Создание события в Google Calendar
        String eventId = googleCalendarService.createEvent(appointment);
        appointment.setGoogleEventId(eventId);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);

        // Отправка подтверждения клиенту
        emailService.sendConfirmationNotification(appointment.getClientEmail(), appointment);
    }
}