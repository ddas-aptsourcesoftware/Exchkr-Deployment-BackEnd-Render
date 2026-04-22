package com.exchkr.club.management.controller;

import com.exchkr.club.management.model.dto.UserDTO;
import com.exchkr.club.management.security.CustomUserDetails;
import com.exchkr.club.management.services.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user-data")
    public UserDTO getUserData(Authentication authentication) {
        // Use the custom principal we built in JwtAuthFilter
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        String email = userDetails.getUsername();
        Long clubId = userDetails.getClubId(); 

        return userService.getUserDataByEmail(email, clubId);
    }
}