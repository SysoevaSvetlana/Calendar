package com.example.Calendar.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleCalendarConfig {

    private static final String APPLICATION_NAME = "My Calendar Integration";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Области доступа к Google Calendar API
     */
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/calendar");

    /**
     * Загрузка учетных данных из credentяials.json
     */
    private GoogleClientSecrets getClientSecrets() throws Exception {
        InputStream in = getClass().getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new Exception("Resource not found: credentяials.json");
        }
        return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    }
    @Bean
    public NetHttpTransport netHttpTransport() {
        return new NetHttpTransport();
    }

    @Bean
    public JacksonFactory jacksonFactory() {
        return JacksonFactory.getDefaultInstance();
    }
    /**
     * Создание OAuth2 Credential, выполняется полный flow, если токены отсутствуют.
     */
    @Bean
    public Credential getCredentials(NetHttpTransport HTTP_TRANSPORT) throws Exception {
        GoogleClientSecrets clientSecrets = getClientSecrets();

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline") // чтобы получить refresh_token
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Bean Google Calendar, готовый к вызовам API
     */
    @Bean
    public Calendar googleCalendar(Credential credential) throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}

