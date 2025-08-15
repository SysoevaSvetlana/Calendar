package com.example.Calendar.service;


import com.example.Calendar.dto.BusySlotDto;
import com.example.Calendar.model.Appointment;
import com.example.Calendar.model.AppointmentStatus;

import com.example.Calendar.repository.AppointmentRepository;
import com.example.Calendar.repository.CalendarConfigRepository;

import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.component.VEvent;
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

    private final CalendarConfigRepository calendarConfigRepository;
    private final EmailService emailService;

    private final YandexCalDavService СalendarService;


    private final ZoneId zone = ZoneId.of("Europe/Moscow");


    @Transactional(readOnly = true)
    public List<BusySlotDto> getBusySlots(LocalDateTime from, LocalDateTime to) throws IOException, ParserException {
        List<BusySlotDto> result = new ArrayList<>();


        List<VEvent> events = СalendarService.getEvents(from, to);
        for (VEvent ev : events) {
            BusySlotDto dto = toDto(ev);
            if (dto != null) {
                result.add(dto);
            }
        }

        return List.copyOf(result);
    }



    private BusySlotDto toDto(VEvent ev) {
        try {
            Instant startInstant = ev.getStartDate().getDate().toInstant();
            Instant endInstant = ev.getEndDate().getDate().toInstant();

            OffsetDateTime start = OffsetDateTime.ofInstant(startInstant, zone);
            OffsetDateTime end = OffsetDateTime.ofInstant(endInstant, zone);

            String title = ev.getSummary() != null ? ev.getSummary().getValue() : "Занято";

            return new BusySlotDto(
                    start.toString(),
                    end.toString(),
                    title,
                    "#ff9f89",      // цвет для занятых слотов
                    "background"    // display для FullCalendar
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

        if (!СalendarService.isSlotAvailable(appointment.getStartTime(), appointment.getEndTime())) {
            throw new IllegalStateException("This slot is already taken (Google Calendar)");
        }

        //что по срокам действия???????

        // Создание события в яндекс Calendar
        String eventId = СalendarService.createEvent(appointment);
        appointment.setGoogleEventId(eventId);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);

        // Отправка подтверждения клиенту
        if (appointment.getClientEmail() != null && !appointment.getClientEmail().isBlank()) {
            emailService.sendConfirmationNotification(appointment.getClientEmail(), appointment);
        }


//        // Отправка подтверждения клиенту
//        emailService.sendConfirmationNotification(appointment.getClientEmail(), appointment);
    }
}