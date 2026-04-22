package com.exchkr.club.management.services.Impl;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import com.exchkr.club.management.model.api.response.StripeAccountResponse;
import com.stripe.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.exchkr.club.management.dao.StripeRepository;
import com.exchkr.club.management.model.entity.Stripe;
import com.exchkr.club.management.model.api.response.StripeResponse;
import com.exchkr.club.management.services.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.net.RequestOptions;
import org.springframework.transaction.annotation.Transactional;


@Service
public class StripeServiceImpl implements StripeService {

    @Autowired
    private StripeRepository stripeRepository;

    @Override
    public StripeResponse createAccountAndOnboard(long userId, long clubId) throws StripeException {
        String stripeAccountId;

        Optional<Stripe> stripeOpt = stripeRepository.findStripeAccountByClubId(clubId);

        if (stripeOpt.isPresent()) {
            stripeAccountId = stripeOpt.get().getStripeAccountId();
        } else {
            // Create new Express account
            Map<String, Object> capabilities = new HashMap<>();
            capabilities.put("card_payments", Map.of("requested", true));
            capabilities.put("transfers", Map.of("requested", true));

            Map<String, Object> params = new HashMap<>();
            params.put("type", "express");
            params.put("country", "US");
            params.put("business_type", "company");
            params.put("capabilities", capabilities);

            Account account = Account.create(params);
            stripeAccountId = account.getId();

            stripeRepository.createAccount(clubId, stripeAccountId);
        }

        // Generate onboarding link
        Map<String, Object> linkParams = new HashMap<>();
        linkParams.put("account", stripeAccountId);
        linkParams.put("refresh_url", "https://aptcarep.com/club-officer/members");
        linkParams.put("return_url", "https://aptcarep.com/club-officer/members");
        linkParams.put("type", "account_onboarding");

        AccountLink link = AccountLink.create(linkParams);

        return new StripeResponse(link.getUrl());
    }


    @Override
    public StripeResponse memberPaymentIntent(
            long clubId,
            long amount,
            String description,
            String paymentMethodType
    ) throws StripeException {

        Optional<String> stripeAccountOpt = stripeRepository.getClubStripeAccountId(clubId);

        if (stripeAccountOpt.isEmpty()) {
            StripeResponse response = new StripeResponse();
            response.setMessage("Stripe not configured for this club");
            return response;
        }

        String connectedAccountId = stripeAccountOpt.get();

        // Configurable platform fee (e.g., 1%)
        long platformFee = Math.round(amount * 0.01);

        Map<String, Object> transferData = new HashMap<>();
        transferData.put("destination", connectedAccountId);

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount); // in cents
        params.put("currency", "usd");
        params.put("description", description);

        // Platform commission
        params.put("application_fee_amount", platformFee);

        // Money goes directly to the club's connected account
        params.put("transfer_data", transferData);

        // Allowed payment method types
        params.put("payment_method_types", Collections.singletonList(paymentMethodType));



        // Create PaymentIntent directly on platform for Direct Charge
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        StripeResponse response = new StripeResponse();
        response.setClientSecret(paymentIntent.getClientSecret());
        response.setPaymentIntentId(paymentIntent.getId());
        response.setStatus(paymentIntent.getStatus());

        return response;
    }


    @Override
    public StripeResponse clubPaymentIntent(
            long userId,
            long amount,
            String description,
            String paymentMethodType
    ) throws StripeException {

        Optional<String> stripeAccountOpt = stripeRepository.getMemberStripeAccountId(userId);

        if (stripeAccountOpt.isEmpty()) {
            StripeResponse response = new StripeResponse();
            response.setMessage("Stripe not configured for this member");
            return response;
        }

        String connectedAccountId = stripeAccountOpt.get();

        // Configurable platform fee (e.g., 1%)
        long platformFee = Math.round(amount * 0.01);

        // Transfer to connected account
        Map<String, Object> transferData = new HashMap<>();
        transferData.put("destination", connectedAccountId);

        // PaymentIntent params
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount); // in cents
        params.put("currency", "usd");
        params.put("description", description);

        // Platform commission
        params.put("application_fee_amount", platformFee);

        // Funds go directly to member's connected account
        params.put("transfer_data", transferData);

        // Allowed payment method types
        params.put("payment_method_types",
                Collections.singletonList(paymentMethodType));


        // Create PaymentIntent on platform (Direct Charge)
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        StripeResponse response = new StripeResponse();
        response.setClientSecret(paymentIntent.getClientSecret());
        response.setPaymentIntentId(paymentIntent.getId());
        response.setStatus(paymentIntent.getStatus());

        return response;
    }

    @Override
    public StripeResponse getStripeBalance(long clubId) {
        StripeResponse response = new StripeResponse();

        // Try to get the Stripe account ID
        Optional<String> optionalAccountId = stripeRepository.getClubStripeAccountId(clubId);
        if (optionalAccountId.isEmpty()) {
            // Club is not connected to Stripe
            response.setMessage("Club is not connected to Stripe");
            return response;
        }

        String stripeAccountId = optionalAccountId.get();

        try {
            RequestOptions requestOptions = RequestOptions.builder()
                    .setStripeAccount(stripeAccountId)
                    .build();

            Balance balance = Balance.retrieve(requestOptions);

            long available = 0;
            long pending = 0;

            if (!balance.getAvailable().isEmpty()) {
                available = balance.getAvailable().get(0).getAmount();
            }

            if (!balance.getPending().isEmpty()) {
                pending = balance.getPending().get(0).getAmount();
            }

            double totalReceivedDollars = (available + pending) / 100.0;

            response.setStripeBalance(totalReceivedDollars);

            response.setBalanceDifference(
                    calculatePercentageDifference(stripeAccountId)
            );

        } catch (StripeException e) {
            // Handle Stripe API errors
            response.setMessage("Failed to retrieve Stripe balance: " + e.getMessage());
        }

        return response;
    }

    public double calculatePercentageDifference(String stripeAccountId)
            throws StripeException {

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        ZonedDateTime currentMonthStart =
                now.withDayOfMonth(1).with(LocalTime.MIN);

        ZonedDateTime lastMonthStart =
                currentMonthStart.minusMonths(1);

        ZonedDateTime lastMonthEnd =
                currentMonthStart.minusSeconds(1);

        RequestOptions options = RequestOptions.builder()
                .setStripeAccount(stripeAccountId)
                .build();

        long lastMonthTotal = 0;
        long currentMonthTotal = 0;

        // ---------- LAST MONTH ----------
        String startingAfter = null;
        do {
            Map<String, Object> params = new HashMap<>(4);
            params.put("limit", 100);
            params.put("created", Map.of(
                    "gte", lastMonthStart.toEpochSecond(),
                    "lte", lastMonthEnd.toEpochSecond()
            ));
            if (startingAfter != null) {
                params.put("starting_after", startingAfter);
            }

            BalanceTransactionCollection collection =
                    BalanceTransaction.list(params, options);

            for (BalanceTransaction tx : collection.getData()) {
                if ("available".equals(tx.getStatus())) {
                    lastMonthTotal += tx.getAmount();
                }
                startingAfter = tx.getId();
            }

            if (!collection.getHasMore()) break;
        } while (true);

        // ---------- CURRENT MONTH ----------
        startingAfter = null;
        do {
            Map<String, Object> params = new HashMap<>(4);
            params.put("limit", 100);
            params.put("created", Map.of(
                    "gte", currentMonthStart.toEpochSecond(),
                    "lte", now.toEpochSecond()
            ));
            if (startingAfter != null) {
                params.put("starting_after", startingAfter);
            }

            BalanceTransactionCollection collection =
                    BalanceTransaction.list(params, options);

            for (BalanceTransaction tx : collection.getData()) {
                if ("available".equals(tx.getStatus())) {
                    currentMonthTotal += tx.getAmount();
                }
                startingAfter = tx.getId();
            }

            if (!collection.getHasMore()) break;
        } while (true);

        // ---------- PERCENTAGE DIFFERENCE ----------
        if (lastMonthTotal == 0) {
            return currentMonthTotal > 0 ? 100.0 : 0.0;
        }

        double percentage =
                ((double) (currentMonthTotal - lastMonthTotal)
                        / lastMonthTotal) * 100;

        return Math.round(percentage * 100.0) / 100.0;
    }


    @Override
    public StripeResponse createMemberAccountAndOnboard(long userId) throws StripeException {

        String stripeAccountId;

        Optional<String> stripeOpt = stripeRepository.findStripeAccountIdByUserId(userId);

        if (stripeOpt.isPresent()) {
            stripeAccountId = stripeOpt.get();
        } else {
            Map<String, Object> capabilities = new HashMap<>();
            capabilities.put("card_payments", Map.of("requested", true));
            capabilities.put("transfers", Map.of("requested", true));

            Map<String, Object> params = new HashMap<>();
            params.put("type", "express");
            params.put("country", "US");
            params.put("business_type", "individual");
            params.put("capabilities", capabilities);

            Account account = Account.create(params);
            stripeAccountId = account.getId();

            stripeRepository.createMemberAccount(userId, stripeAccountId);
        }

        Map<String, Object> linkParams = new HashMap<>();
        linkParams.put("account", stripeAccountId);
        linkParams.put("refresh_url", "https://aptcarep.com/club-member/payments");
        linkParams.put("return_url", "https://aptcarep.com/club-member/payments");
        linkParams.put("type", "account_onboarding");

        AccountLink link = AccountLink.create(linkParams);

        return new StripeResponse(link.getUrl());
    }



    @Override
    @Transactional
    public ResponseEntity<StripeAccountResponse> getClubStripeInfo(Long clubId) {
        try {
            // Fetch the row (native query returns List<Object[]>)
            List<Object[]> rows = stripeRepository.getClubStripeInfo(clubId);

            StripeAccountResponse response;

            if (rows.isEmpty()) {
                // No Stripe account found, return default values
                response = new StripeAccountResponse(
                        "Not configured",
                        false,
                        false
                );
            } else {
                Object[] row = rows.get(0);

                String stripeAccountStatus = row[0] != null ? row[0].toString() : "Not configured";
                Boolean payoutsEnabled = row[1] != null ? (Boolean) row[1] : false;
                Boolean chargesEnabled = row[2] != null ? (Boolean) row[2] : false;

                response = new StripeAccountResponse(
                        stripeAccountStatus,
                        payoutsEnabled,
                        chargesEnabled
                );
            }

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            // Return 500 on unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<StripeAccountResponse> getMemberStripeInfo(long userId) {
        try {
            // Fetch the row (native query returns List<Object[]>)
            List<Object[]> rows = stripeRepository.getMemberStripeInfo(userId);

            StripeAccountResponse response;

            if (rows.isEmpty()) {
                // No Stripe account found, return default values
                response = new StripeAccountResponse(
                        "Not configured",
                        false,
                        false
                );
            } else {
                Object[] row = rows.get(0);

                String stripeAccountStatus = row[0] != null ? row[0].toString() : "Not configured";
                Boolean payoutsEnabled = row[1] != null ? (Boolean) row[1] : false;
                Boolean chargesEnabled = row[2] != null ? (Boolean) row[2] : false;

                response = new StripeAccountResponse(
                        stripeAccountStatus,
                        payoutsEnabled,
                        chargesEnabled
                );
            }

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            // Return 500 on unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public StripeResponse donationPaymentIntent(
            long clubId,
            long amount,
            String description,
            String paymentMethodType
    ) throws StripeException {

        Optional<String> stripeAccountOpt = stripeRepository.getClubStripeAccountId(clubId);

        if (stripeAccountOpt.isEmpty()) {
            StripeResponse response = new StripeResponse();
            response.setMessage("Stripe not configured for this club");
            return response;
        }

        String connectedAccountId = stripeAccountOpt.get();

        // Configurable platform fee (e.g., 1%)
        long platformFee = Math.round(amount * 0.01);

        Map<String, Object> transferData = new HashMap<>();
        transferData.put("destination", connectedAccountId);

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount); // in cents
        params.put("currency", "usd");
        params.put("description", description);

        // Platform commission
        params.put("application_fee_amount", platformFee);

        // Money goes directly to the club's connected account
        params.put("transfer_data", transferData);

        // Allowed payment method types
        params.put("payment_method_types", Collections.singletonList(paymentMethodType));



        // Create PaymentIntent directly on platform for Direct Charge
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        StripeResponse response = new StripeResponse();
        response.setClientSecret(paymentIntent.getClientSecret());
        response.setPaymentIntentId(paymentIntent.getId());
        response.setStatus(paymentIntent.getStatus());

        return response;
    }

}
