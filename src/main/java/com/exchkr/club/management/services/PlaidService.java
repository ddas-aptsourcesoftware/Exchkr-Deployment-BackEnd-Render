package com.exchkr.club.management.services;

import com.exchkr.club.management.model.api.request.PlaidExchangeRequest;
import com.plaid.client.model.AccountBase;
import java.util.List;
import java.util.Map;

public interface PlaidService {

    /**
     * Retrieves the current link status (isLinked, institutionName, etc.) for a club.
     */
    Map<String, Object> getLinkStatus(Long clubId);

    /**
     * Generates a link token. Now uses userId directly from the security context.
     */
    String createLinkToken(Long userId, Long clubId) throws Exception;
    
    /**
     * Re-activate a user's account connection.
     */
    void reactivateAccount(Long clubId) throws Exception;

    /**
     * Exchanges a public token for an access token and saves it.
     */
    String exchangePublicToken(PlaidExchangeRequest request, Long userId, Long clubId) throws Exception;

    /**
     * Fetches real-time balances from Plaid. 
     * No longer needs userId as it fetches by the club's active access token.
     */
    List<AccountBase> getAccountBalance(Long clubId) throws Exception;

    /**
     * Updates the specific account ID within a Plaid Item used for club operations.
     */
    void updateDefaultAccount(Long clubId, String newAccountId);

    /**
     * Removes the link between the club and the bank institution.
     */
    void unlinkAccount(Long clubId) throws Exception;
}