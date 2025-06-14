package com.example.Calendar.controller;

import com.example.Calendar.model.CalendarConfig;
import com.example.Calendar.service.CalendarConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar/config")
@RequiredArgsConstructor
public class CalendarConfigController {

    private final CalendarConfigService configService;

    @GetMapping
    public CalendarConfig getConfig() {
        return configService.getCalendarConfig();
    }

    @PutMapping("/working-hours")
    public void updateWorkingHours(
            @RequestParam int startHour,
            @RequestParam int endHour) {
        configService.updateWorkingHours(startHour, endHour);
    }
}