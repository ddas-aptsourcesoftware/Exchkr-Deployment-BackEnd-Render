package com.exchkr.club.management.services;

import com.exchkr.club.management.model.api.request.AccountRecoveryRequest;

public interface AccountRecoveryService {
    void resetPassword(AccountRecoveryRequest request);
}