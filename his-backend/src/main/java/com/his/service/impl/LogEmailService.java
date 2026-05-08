package com.his.service.impl;

import com.his.entity.User;
import com.his.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "his.mail.provider", havingValue = "log", matchIfMissing = true)
public class LogEmailService implements EmailService {

    @Value("${his.app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public void sendEmailVerification(User user, String verificationToken) {
        log.info("E-posta doğrulama bağlantısı [{}]: {}/api/auth/verify-email?token={}",
                user.getEmail(), baseUrl, verificationToken);
    }

    @Override
    public void sendPasswordReset(User user, String resetToken) {
        log.info("Şifre sıfırlama token [{}]: {}", user.getEmail(), resetToken);
    }
}
