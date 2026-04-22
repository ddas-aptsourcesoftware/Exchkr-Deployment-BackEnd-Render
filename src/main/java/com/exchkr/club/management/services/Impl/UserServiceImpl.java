package com.exchkr.club.management.services.Impl;

import com.exchkr.club.management.dao.UserRepository;
import com.exchkr.club.management.dao.UserClubRepository; 
import com.exchkr.club.management.dao.UserClubMembershipProjection;
import com.exchkr.club.management.model.entity.User;
import com.exchkr.club.management.model.dto.UserDTO;
import com.exchkr.club.management.services.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserClubRepository userClubRepository; 

    public UserServiceImpl(UserRepository userRepository, UserClubRepository userClubRepository) {
        this.userRepository = userRepository;
        this.userClubRepository = userClubRepository;
    }

    @Override
    public UserDTO getUserDataByEmail(String email, Long clubId) {
        // 1. Fetch the user entity
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 2. Use the projection to get specific membership details (Role and Club Name)
        UserClubMembershipProjection membership = userClubRepository.findMembershipDetail(user.getUserId(), clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No active membership for this club context"));

        // 3. Build the DTO using the data from both the User and the Membership Projection
        List<String> roles = Collections.singletonList(membership.getRoleName());
        String clubName = membership.getClubName();

        return UserDTO.fromUser(user, roles, clubName, clubId, membership.getJoinedAt());
    }
}