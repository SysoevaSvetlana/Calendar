package com.example.Calendar.service;


import com.example.Calendar.model.Appointment;
import com.example.Calendar.model.CalendarConfig;
import com.example.Calendar.repository.CalendarConfigRepository;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.api.client.util.DateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.time.*;

@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final Calendar googleCalendar;
    private final CalendarConfigRepository calendarConfigRepository;
    private final CalendarConfigService configService;
    private final ZoneId zone = ZoneId.of("Europe/Moscow");


    public List<Event> getEvents(LocalDateTime start, LocalDateTime end) throws IOException {
        CalendarConfig cfg = configService.getCalendarConfig();
        DateTime timeMin = new DateTime(Date.from(start.atZone(zone).toInstant()));
        DateTime timeMax = new DateTime(Date.from(end.atZone(zone).toInstant()));

        Events events = googleCalendar.events()
                .list(cfg.getCalendarId())
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();
        return events.getItems();
    }


    public String createEvent(Appointment appointment) throws IOException {
        //CalendarConfig config = calendarConfigRepository.findByCalendarId("primary")
        CalendarConfig config = configService.getCalendarConfig();
        //.orElseThrow(() -> new IllegalStateException("Calendar config not found"));

        Event event = new Event()
                .setSummary("Appointment with " + appointment.getClientName())
                .setDescription(appointment.getDescription());

        // Установка времени начала и окончания
        event.setStart(new EventDateTime()
                .setDateTime(toGoogleDateTime(appointment.getStartTime(), config.getTimeZone())));

        event.setEnd(new EventDateTime()
                .setDateTime(toGoogleDateTime(appointment.getEndTime(), config.getTimeZone())));

        // Создание события в календаре
        Event createdEvent = googleCalendar.events()
                .insert(config.getCalendarId(), event)
                .execute();
        //узнать доп параметры

        return createdEvent.getId();
    }

    // Проверить, свободен ли слот (учитывая GCal)
    public boolean isSlotAvailable(LocalDateTime start, LocalDateTime end) throws IOException {
        List<Event> events = getEvents(start.minusMinutes(1), end.plusMinutes(1));
        for (Event event : events) {
            LocalDateTime eStart = LocalDateTime.parse(event.getStart().getDateTime().toStringRfc3339());
            LocalDateTime eEnd = LocalDateTime.parse(event.getEnd().getDateTime().toStringRfc3339());

            if (eStart.isBefore(end) && eEnd.isAfter(start)) {
                return false;
            }
        }
        return true;
    }

    private DateTime toGoogleDateTime(LocalDateTime localDateTime, String timeZone) {
        Date date = Date.from(localDateTime.atZone(ZoneId.of(timeZone)).toInstant());
        return new DateTime(date);
    }
}