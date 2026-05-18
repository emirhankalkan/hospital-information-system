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

    @Value("${his.app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendEmailVerification(User user, String verificationToken) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("HIS e-posta dogrulama");
        message.setText("""
                Merhaba %s,

                Hospital Information System hesabinizi dogrulamak icin asagidaki baglantiyi acin:

                %s

                Bu baglanti sureli olarak gecerlidir.
                """.formatted(user.getUsername(), verificationUrl));

        mailSender.send(message);
    }

    @Override
    public void sendPasswordReset(User user, String resetToken) {
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("HIS sifre sifirlama");
        message.setText("""
                Merhaba %s,

                Sifrenizi sifirlamak icin asagidaki baglantiyi acin:

                %s

                Bu baglanti sureli olarak gecerlidir.
                """.formatted(user.getUsername(), resetUrl));

        mailSender.send(message);
    }
}
