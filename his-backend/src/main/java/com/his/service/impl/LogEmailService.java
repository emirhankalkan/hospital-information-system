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

    @Value("${his.app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void sendEmailVerification(User user, String verificationToken) {
        log.info("E-posta dogrulama baglantisi [{}]: {}/verify-email?token={}",
                user.getEmail(), frontendUrl, verificationToken);
    }

    @Override
    public void sendPasswordReset(User user, String resetToken) {
        log.info("Sifre sifirlama baglantisi [{}]: {}/reset-password?token={}",
                user.getEmail(), frontendUrl, resetToken);
    }
}
