package com.his.service;

import com.his.dto.request.LoginRequest;
import com.his.dto.request.RegisterRequest;
import com.his.dto.response.JwtResponse;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    void register(RegisterRequest request);
}
