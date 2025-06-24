package com.example.Calendar.service;


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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

//    @Transactional(readOnly = true)
//    public List<LocalDateTime[]> getUnavailableSlots(LocalDateTime start,
//                                                     LocalDateTime end) throws IOException {
//
//        List<LocalDateTime[]> result = new ArrayList<>();
//
//
//        appointmentRepository.findConfirmedBetweenDates(start, end)
//                .forEach(a -> result.add(new LocalDateTime[]{
//                        a.getStartTime(), a.getEndTime()
//                }));
//
//
//        ZoneId tz = ZoneId.of("Europe/Moscow");     // или вытянуть из CalendarConfig
//
//        googleCalendarService.getEvents(start, end)
//                .forEach(ev -> {
//                    LocalDateTime gStart = toLocal(ev.getStart(), tz);
//                    LocalDateTime gEnd   = toLocal(ev.getEnd(),   tz);
//                    result.add(new LocalDateTime[]{gStart, gEnd});
//                });
//
//        return result;
//    }
//    private LocalDateTime toLocal(EventDateTime edt, ZoneId zone) {
//        /*
//         * edt.getDateTime()  → com.google.api.client.util.DateTime
//         * .getValue()        → long millis since epoch
//         */
//        return Instant.ofEpochMilli(edt.getDateTime().getValue())
//                .atZone(zone)
//                .toLocalDateTime();
//    }

//    @Transactional(readOnly = true)
//    public List<Appointment> getAvailableSlots(LocalDateTime start, LocalDateTime end) {
//        return appointmentRepository.findConfirmedBetweenDates(start, end);


    @Transactional(readOnly = true)
    public List<LocalDateTime[]> getUnavailableSlots(LocalDateTime start,
                                                     LocalDateTime end) throws IOException {

        List<LocalDateTime[]> result = new ArrayList<>();
        ZoneId zone = ZoneId.of("Europe/Moscow");

        googleCalendarService.getEvents(start, end).forEach(event -> {
            if (event.getStart().getDateTime() != null && event.getEnd().getDateTime() != null) {
                LocalDateTime gStart = toLocal(event.getStart(), zone);
                LocalDateTime gEnd = toLocal(event.getEnd(), zone);
                result.add(new LocalDateTime[]{gStart, gEnd});
            }
        });

        return result;
    }

    // Преобразуем EventDateTime → LocalDateTime
    private LocalDateTime convertEventDateTimeToLocal(EventDateTime edt, ZoneId zone) {
        if (edt.getDateTime() != null) {
            // Событие с точным временем
            DateTime dt = edt.getDateTime();
            return Instant.ofEpochMilli(dt.getValue())
                    .atZone(zone)
                    .toLocalDateTime();
        } else if (edt.getDate() != null) {
            // Весь день (all-day) событие — дата без времени
            DateTime dt = edt.getDate();
            LocalDate date = LocalDate.parse(dt.toStringRfc3339());
            return date.atStartOfDay();
        } else {
            // Неизвестный формат — возвращаем null или кидаем исключение
            throw new IllegalArgumentException("EventDateTime не содержит ни dateTime, ни date");
        }
    }




//    }
//    @Transactional(readOnly = true)
//    public List<LocalDateTime[]> getUnavailableSlots(LocalDateTime start, LocalDateTime end) throws IOException {
//        List<LocalDateTime[]> result = new ArrayList<>();
//
//        // Слоты из БД
//        List<Appointment> confirmed = appointmentRepository.findConfirmedBetweenDates(start, end);
//        for (Appointment a : confirmed) {
//            result.add(new LocalDateTime[]{a.getStartTime(), a.getEndTime()});
//        }
//
//        // Слоты из Google Calendar
//        List<Event> events = googleCalendarService.getEvents(start, end);
//        for (Event e : events) {
//            LocalDateTime eStart = LocalDateTime.parse(e.getStart().getDateTime().toStringRfc3339());
//            LocalDateTime eEnd = LocalDateTime.parse(e.getEnd().getDateTime().toStringRfc3339());
//            result.add(new LocalDateTime[]{eStart, eEnd});
//        }
//
//        return result;
//    }

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