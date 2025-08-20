package com.example.Calendar.config;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void init() {
        // Устанавливаем московскую временную зону для всего приложения
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
    }
}
