package com.his.service;

import com.his.dto.request.LoginRequest;
import com.his.dto.request.ForgotPasswordRequest;
import com.his.dto.request.RefreshTokenRequest;
import com.his.dto.request.RegisterRequest;
import com.his.dto.request.ResendVerificationRequest;
import com.his.dto.request.ResetPasswordRequest;
import com.his.dto.response.JwtResponse;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    void register(RegisterRequest request);
    JwtResponse refreshToken(RefreshTokenRequest request);
    void logout(RefreshTokenRequest request);
    void verifyEmail(String token);
    void resendVerification(ResendVerificationRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
