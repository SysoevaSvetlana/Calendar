package com.example.Calendar.controller;


import com.example.Calendar.model.Appointment;
import com.example.Calendar.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    //    @GetMapping("/slots")
//    public List<Appointment> getAvailableSlots(
//            @RequestParam LocalDateTime start,
//            @RequestParam LocalDateTime end) {
//        return appointmentService.getAvailableSlots(start, end);
//    }
//    @GetMapping("/slots")
//    public List<LocalDateTime[]> getUnavailableSlots(
//            @RequestParam OffsetDateTime start,
//            @RequestParam OffsetDateTime end) throws IOException {
//
//        return appointmentService.getUnavailableSlots(
//                start.toLocalDateTime(),
//                end.toLocalDateTime());
//    }

    @GetMapping("/slots")
    public List<Map<String, Object>> getUnavailableSlots(
            @RequestParam OffsetDateTime start,
            @RequestParam OffsetDateTime end) throws IOException {

        List<LocalDateTime[]> busySlots = appointmentService.getUnavailableSlots(
                start.toLocalDateTime(), end.toLocalDateTime());

        return busySlots.stream().map(slot -> {
            Map<String, Object> event = new HashMap<>();
            event.put("start", slot[0].toString()); // ISO 8601
            event.put("end", slot[1].toString());
            event.put("title", "Занято");
            event.put("display", "background");
            event.put("backgroundColor", "#d3d3d3");
            return event;
        }).collect(Collectors.toList());
    }


    @PostMapping
    public Appointment createAppointment(
            @RequestBody AppointmentRequest request) {
        System.out.println("Получен запрос: clientEmail=" + request.clientEmail());
        return appointmentService.createAppointment(
                request.toAppointment(),
                request.ownerEmail()
        );
    }

    @PostMapping("/confirm")
    public void confirmAppointment(@RequestParam UUID token) throws IOException {
        appointmentService.confirmAppointment(token);
    }

    public record AppointmentRequest(
            String clientName,
            String clientEmail,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String description,
            String ownerEmail
    ) {
        public Appointment toAppointment() {
            Appointment appointment = new Appointment();
            appointment.setClientName(clientName);
            appointment.setClientEmail(clientEmail);
            appointment.setStartTime(startTime);
            appointment.setEndTime(endTime);
            appointment.setDescription(description);
            return appointment;
        }
    }
}