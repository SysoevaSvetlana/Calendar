package com.example.Calendar.service;


import com.example.Calendar.model.Appointment;
import com.example.Calendar.model.Notification;
import com.example.Calendar.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepo;

    public void sendConfirmationRequest(String ownerEmail, Appointment appointment) {
        System.out.println("Отправка письма владельцу для appointment с packageName: " + appointment.getPackageName());

        String confirmationUrl = "http://localhost:8080/api/appointments/confirm?token=" + appointment.getConfirmationToken();

        Context context = new Context();
        context.setVariable("appointment", appointment);
        context.setVariable("confirmationUrl", confirmationUrl);

        String htmlContent = templateEngine.process("email/confirmation-request", context);

        sendEmail(ownerEmail, "New Appointment Request", htmlContent);
    }

    public void sendConfirmationNotification(String clientEmail, Appointment appointment) {
        System.out.println("Отправка письма клиенту для appointment с packageName: " + appointment.getPackageName());

        Context context = new Context();
        context.setVariable("appointment", appointment);

        String htmlContent = templateEngine.process("email/confirmation-notification", context);

        sendEmail(clientEmail, "Your Appointment Confirmed", htmlContent);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            // 2. запись в БД
            Notification n = new Notification();
            n.setRecipientEmail(to);
            n.setSubject(subject);
            n.setContent( htmlContent);
            n.setSentAt(LocalDateTime.now());
            notificationRepo.save(n);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}