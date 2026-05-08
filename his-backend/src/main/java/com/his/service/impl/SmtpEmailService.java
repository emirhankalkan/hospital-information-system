package com.his.service.impl;

import com.his.entity.User;
import com.his.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "his.mail.provider", havingValue = "smtp")
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${his.app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendEmailVerification(User user, String verificationToken) {
        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + verificationToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("HIS e-posta doğrulama");
        message.setText("""
                Merhaba %s,

                Hospital Information System hesabınızı doğrulamak için aşağıdaki bağlantıyı açın:

                %s

                Bu bağlantı süreli olarak geçerlidir.
                """.formatted(user.getUsername(), verificationUrl));

        mailSender.send(message);
    }

    @Override
    public void sendPasswordReset(User user, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("HIS şifre sıfırlama");
        message.setText("""
                Merhaba %s,

                Şifrenizi sıfırlamak için Swagger'da /api/auth/reset-password endpoint'ine aşağıdaki token ile istek gönderin:

                %s

                Bu token süreli olarak geçerlidir.
                """.formatted(user.getUsername(), resetToken));

        mailSender.send(message);
    }
}
