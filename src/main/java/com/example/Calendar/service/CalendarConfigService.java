package com.example.Calendar.service;


import com.example.Calendar.model.CalendarConfig;
import com.example.Calendar.repository.CalendarConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalendarConfigService {

    private final CalendarConfigRepository calendarConfigRepository;

    //    @Transactional(readOnly = true)
//    public CalendarConfig getCalendarConfig() {
//        return calendarConfigRepository.findByCalendarId("primary")
//                .orElseThrow(() -> new IllegalStateException("Calendar config not found"));
//    }
    @Transactional(readOnly = true)
    public CalendarConfig getCalendarConfig() {

        return calendarConfigRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Config not found"));
    }

    @Transactional
    public void updateWorkingHours(int startHour, int endHour) {

        calendarConfigRepository.updateWorkingHours(getCalendarConfig().getCalendarId(), startHour, endHour);
    }
}
