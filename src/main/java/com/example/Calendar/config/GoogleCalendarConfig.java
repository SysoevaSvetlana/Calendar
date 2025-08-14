package com.example.Calendar.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.services.calendar.Calendar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleCalendarConfig {

    private static final String APPLICATION_NAME = "My Calendar Integration";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    // Доступы только к календарю
    private static final List<String> SCOPES =
            Collections.singletonList("https://www.googleapis.com/auth/calendar");

    /**
     * Загружаем credentials.json (скачан из Google Cloud Console для OAuth client ID)
     */
    private GoogleClientSecrets getClientSecrets() throws Exception {
        InputStream in = getClass().getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new Exception("Resource not found: /credentials.json");
        }
        return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    }

    /**
     * Возвращает Credential, который либо берётся из сохранённых токенов,
     * либо запускает браузер для однократной авторизации владельца.
     */
    @Bean
    public Credential ownerCredential() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleClientSecrets clientSecrets = getClientSecrets();

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(Paths.get(TOKENS_DIRECTORY_PATH).toFile()))
                .setAccessType("offline") // обязательно, чтобы был refresh_token
                .setApprovalPrompt("force") // иногда нужно, чтобы refresh_token выдался
                .build();

        // "owner" — фиксированный ID владельца, не будет новых пользователей
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver.Builder().setPort(8888).build())
                .authorize("owner");
    }

    /**
     * Готовый бин Calendar, от имени владельца
     */
    @Bean
    public Calendar googleCalendar(Credential ownerCredential)
            throws GeneralSecurityException, Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(httpTransport, JSON_FACTORY, ownerCredential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
