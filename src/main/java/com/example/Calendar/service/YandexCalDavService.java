package com.example.Calendar.service;

import com.example.Calendar.config.YandexCalDavConfig;
import com.example.Calendar.model.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import net.fortuna.ical4j.util.CompatibilityHints;
import net.fortuna.ical4j.validate.ValidationException;
import net.fortuna.ical4j.model.Property;

import net.fortuna.ical4j.data.ParserException;

@Service
@RequiredArgsConstructor
public class YandexCalDavService {

    private final YandexCalDavConfig config;
    private final ZoneId zone = ZoneId.of("Europe/Moscow");

    private Sardine sardine() {
        return SardineFactory.begin(config.getUsername(), config.getPassword());
    }

    public boolean isSlotAvailable(LocalDateTime start, LocalDateTime end) throws IOException {
        List<VEvent> events = getEvents(start.minusMinutes(1), end.plusMinutes(1));
        return events.isEmpty();
    }

    public List<VEvent> getEvents(LocalDateTime start, LocalDateTime end) throws IOException {
//        String calendarUrl = config.getUrl();
        String calendarUrl = config.getUrl() + config.getCalendarPath();
//        if (!calendarUrl.endsWith("/")) calendarUrl += "/";
//        calendarUrl += config.getCalendarPath().startsWith("/") ? config.getCalendarPath().substring(1) : config.getCalendarPath();

//        System.out.println("URL из конфига: " + config.getUrl());
//        System.out.println("Путь из конфига: " + config.getCalendarPath());
//        System.out.println("Логин: " + config.getUsername());

        System.out.println("Calendar URL: " + calendarUrl);
        Sardine s = sardine();
        List<DavResource> resources = s.list(calendarUrl, 1, false);
        List<VEvent> result = new ArrayList<>();

        for (DavResource res : resources) {
            if (res.getName().endsWith(".ics")) {
                try (InputStream is = s.get(calendarUrl + res.getName())) {
                    CalendarBuilder cb = new CalendarBuilder();
                    net.fortuna.ical4j.model.Calendar ical = cb.build(is);
                    for (Component comp : ical.getComponents(Component.VEVENT)) {
                        VEvent ve = (VEvent) comp;
                        Instant eventStartInstant = ve.getStartDate().getDate().toInstant();
                        LocalDateTime eventStart = LocalDateTime.ofInstant(eventStartInstant, zone);

                        Instant eventEndInstant = ve.getEndDate().getDate().toInstant();
                        LocalDateTime eventEnd = LocalDateTime.ofInstant(eventEndInstant, zone);

                        if (!eventEnd.isBefore(start) && !eventStart.isAfter(end)) {
                            result.add(ve);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    public String createEvent(Appointment appointment) throws IOException, ValidationException {
        System.out.println("Создание события для appointment с packageName: " + appointment.getPackageName());

        String calendarUrl = config.getUrl() + config.getCalendarPath();
        if (!calendarUrl.endsWith("/")) {
            calendarUrl += "/";
        }

        Sardine sardine = SardineFactory.begin(config.getUsername(), config.getPassword());

        String uid = UUID.randomUUID().toString();

        DateTime startDate = new DateTime(Date.from(appointment.getStartTime().atZone(zone).toInstant()));
        DateTime endDate   = new DateTime(Date.from(appointment.getEndTime().atZone(zone).toInstant()));

        VEvent event = new VEvent(startDate, endDate, "Запись: " + appointment.getClientName());
        event.getProperties().add(new Uid(uid));
        event.getProperties().add(new Description(String.format(
                "Имя: %s%nEmail: %s%nТелефон: %s%nПакет: %s%nКомментарий: %s",
                appointment.getClientName(),
                appointment.getClientEmail(),
                appointment.getClientPhone(),
                appointment.getPackageName() != null ? appointment.getPackageName() : "Не указан",
                appointment.getDescription() != null ? appointment.getDescription() : ""
        )));

        net.fortuna.ical4j.model.Calendar ical = new net.fortuna.ical4j.model.Calendar();
        ical.getProperties().add(new ProdId("-//MyApp//iCal4j 1.0//RU"));
        ical.getProperties().add(Version.VERSION_2_0);
        ical.getProperties().add(CalScale.GREGORIAN);
        ical.getComponents().add(event);

        // Сохраняем календарь в массив байтов
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CalendarOutputter().output(ical, baos);
        byte[] icalBytes = baos.toByteArray();

        // Создаём новый InputStream для PUT
        String eventUrl = calendarUrl + uid + ".ics";
        sardine.put(eventUrl, baos.toByteArray());

//        try (ByteArrayInputStream bais = new ByteArrayInputStream(icalBytes)) {
//            sardine.put(eventUrl, bais, "text/calendar");
//        }

        return uid;
    }

//    public String createEvent(Appointment appointment) throws IOException, ValidationException {
//        String calendarUrl = config.getUrl() + config.getCalendarPath();
//        if (!calendarUrl.endsWith("/")) {
//            calendarUrl += "/";
//        }
//
//        Sardine sardine = SardineFactory.begin(config.getUsername(), config.getPassword());
//
//        String uid = UUID.randomUUID().toString();
//
//        DateTime startDate = new DateTime(Date.from(appointment.getStartTime().atZone(zone).toInstant()));
//        DateTime endDate   = new DateTime(Date.from(appointment.getEndTime().atZone(zone).toInstant()));
//
//        VEvent event = new VEvent(startDate, endDate, "Запись: " + appointment.getClientName());
//        event.getProperties().add(new Uid(uid));
//        event.getProperties().add(new Description(String.format(
//                "Имя: %s%nEmail: %s%nТелефон: %s%nКомментарий: %s",
//                appointment.getClientName(),
//                appointment.getClientEmail(),
//                appointment.getClientPhone(),
//                appointment.getDescription() != null ? appointment.getDescription() : ""
//        )));
//
//        net.fortuna.ical4j.model.Calendar ical = new net.fortuna.ical4j.model.Calendar();
//        ical.getProperties().add(new ProdId("-//MyApp//iCal4j 1.0//RU"));
//        ical.getProperties().add(Version.VERSION_2_0);
//        ical.getProperties().add(CalScale.GREGORIAN);
//        ical.getComponents().add(event);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        new CalendarOutputter().output(ical, baos);
//
//        String eventUrl = calendarUrl + uid + ".ics";
//        sardine.put(eventUrl, new ByteArrayInputStream(baos.toByteArray()), "text/calendar");
//
//        return uid;
//    }
//

//    public String createEvent(Appointment appointment) throws IOException {
//        String calendarUrl = config.getUrl() + config.getCalendarPath();
//        Sardine s = sardine();
//
//        // UID
//        String uid = UUID.randomUUID().toString();
//
//        // Преобразуем LocalDateTime в iCal4j DateTime
//        DateTime startDate = new DateTime(Date.from(appointment.getStartTime().atZone(zone).toInstant()));
//        DateTime endDate = new DateTime(Date.from(appointment.getEndTime().atZone(zone).toInstant()));
//
//        // Создаём событие
//        VEvent event = new VEvent(startDate, endDate, "Запись: " + appointment.getClientName());
//        event.getProperties().add(new Uid(uid));
//        event.getProperties().add(new Description(
//                String.format("Имя: %s%nEmail: %s%nТелефон: %s%nКомментарий: %s",
//                        appointment.getClientName(),
//                        appointment.getClientEmail(),
//                        appointment.getClientPhone(),
//                        appointment.getDescription() != null ? appointment.getDescription() : "")
//        ));
//
//        // Создаём календарь
//        net.fortuna.ical4j.model.Calendar ical = new net.fortuna.ical4j.model.Calendar();
//        ical.getProperties().add(new ProdId("-//MyApp//iCal4j 1.0//RU"));
//        ical.getProperties().add(Version.VERSION_2_0);
//        ical.getProperties().add(CalScale.GREGORIAN);
//        ical.getComponents().add(event);
//
//        // Записываем .ics файл на сервер Яндекс.Календаря
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        new CalendarOutputter().output(ical, baos);
//
//        String eventPath = calendarUrl + uid + ".ics";
//        s.put(eventPath, new ByteArrayInputStream(baos.toByteArray()));
//
//        return uid;
//    }
}

