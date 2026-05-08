package com.his.service;

import com.his.entity.User;

public interface EmailService {

    void sendEmailVerification(User user, String verificationToken);

    void sendPasswordReset(User user, String resetToken);
}
