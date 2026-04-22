package com.exchkr.club.management.services;

import com.exchkr.club.management.model.dto.UserDTO;

public interface UserService {

    /**
     * Fetch user data enriched with Role and Club Name 
     * based on the currently active club context.
     */
    UserDTO getUserDataByEmail(String email, Long clubId);

}