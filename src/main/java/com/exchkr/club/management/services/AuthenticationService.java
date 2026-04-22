package com.exchkr.club.management.services;

import com.exchkr.club.management.model.api.request.AuthRequest;
import com.exchkr.club.management.model.api.response.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    LoginResponse login(AuthRequest request, HttpServletResponse response); 
    
    LoginResponse refreshToken(String refreshToken, HttpServletResponse response);
    
    LoginResponse selectClub(Long userId, Long clubId, HttpServletResponse response);
}