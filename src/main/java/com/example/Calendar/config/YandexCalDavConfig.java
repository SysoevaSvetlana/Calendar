package com.example.Calendar.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "yandex.caldav")
@Data
public class YandexCalDavConfig {
    private String url;
    private String username;
    private String password;
    private String calendarPath;
    @PostConstruct
    public void debugPrint() {
        System.out.println("Loaded config: " + this);
    }
}
