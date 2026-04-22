package com.exchkr.club.management.dao;

import com.exchkr.club.management.model.entity.User;

public interface UserWithClubProjection {
    User getUser();
    String getClubName();
}