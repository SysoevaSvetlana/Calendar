package com.example.Calendar.controller;


import com.example.Calendar.dto.BusySlotDto;
import com.example.Calendar.model.Appointment;
import com.example.Calendar.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.data.ParserException;
import org.springframework.http.ResponseEntity;
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


    @GetMapping("/slots")
        public List<BusySlotDto> slots(@RequestParam OffsetDateTime start,
                @RequestParam OffsetDateTime end) throws IOException, ParserException {


            LocalDateTime from = start.toLocalDateTime();
            LocalDateTime to = end.toLocalDateTime();

            return appointmentService.getBusySlots(from, to);
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

    @GetMapping("/confirm")
    public ResponseEntity<Void> confirmAppointment(@RequestParam UUID token) throws IOException {
        appointmentService.confirmAppointment(token);
        return ResponseEntity.ok().build();
    }

    public record AppointmentRequest(
            String clientName,
            String clientEmail,
            String clientPhone,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String description,
            String ownerEmail
    ) {
        public Appointment toAppointment() {
            Appointment appointment = new Appointment();
            appointment.setClientName(clientName);
            appointment.setClientEmail(clientEmail);
            appointment.setClientPhone(clientPhone);
            appointment.setStartTime(startTime);
            appointment.setEndTime(endTime);
            appointment.setDescription(description);
            return appointment;
        }
    }
}