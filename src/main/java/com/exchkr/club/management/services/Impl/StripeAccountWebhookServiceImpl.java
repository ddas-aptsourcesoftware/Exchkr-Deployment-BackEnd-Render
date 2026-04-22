package com.exchkr.club.management.services.Impl;

import com.exchkr.club.management.services.StripeAccountWebhookService;
import com.exchkr.club.management.dao.StripeAccountWebhookRepository;
import com.stripe.model.Account;
import com.stripe.model.Event;
import com.stripe.net.ApiResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StripeAccountWebhookServiceImpl implements StripeAccountWebhookService {

    @Autowired
    private StripeAccountWebhookRepository stripeAccountWebhookRepository;

    @Override
    public void processEvent(Event event) {
        try {
            switch (event.getType()) {
                case "account.updated":
                    handleAccountUpdated(event);
                    break;
                default:
                    // intentionally ignored
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void handleAccountUpdated(Event event) {

        Account account = (Account) ApiResource.GSON.fromJson(
                event.getDataObjectDeserializer().getRawJson(),
                Account.class
        );

        boolean isEnabled =
                Boolean.TRUE.equals(account.getDetailsSubmitted()) &&
                        Boolean.TRUE.equals(account.getChargesEnabled()) &&
                        Boolean.TRUE.equals(account.getPayoutsEnabled());

        boolean isRestricted =
                account.getRequirements() != null &&
                        account.getRequirements().getDisabledReason() != null;

        String currentStatus =
                isEnabled ? "enabled" : (isRestricted ? "restricted" : "pending");

        Map<String, Object> previousAttributes = event.getData().getPreviousAttributes();
        String previousStatus = null;

        if (previousAttributes != null && !previousAttributes.isEmpty()) {

            Boolean prevDetailsSubmitted =
                    (Boolean) previousAttributes.getOrDefault(
                            "details_submitted",
                            account.getDetailsSubmitted()
                    );

            Boolean prevChargesEnabled =
                    (Boolean) previousAttributes.getOrDefault(
                            "charges_enabled",
                            account.getChargesEnabled()
                    );

            Boolean prevPayoutsEnabled =
                    (Boolean) previousAttributes.getOrDefault(
                            "payouts_enabled",
                            account.getPayoutsEnabled()
                    );

            Object prevDisabledReason =
                    ((Map<?, ?>) previousAttributes
                            .getOrDefault("requirements", Map.of()))
                            .get("disabled_reason");

            boolean prevEnabled =
                    Boolean.TRUE.equals(prevDetailsSubmitted) &&
                            Boolean.TRUE.equals(prevChargesEnabled) &&
                            Boolean.TRUE.equals(prevPayoutsEnabled);

            boolean prevRestricted = prevDisabledReason != null;

            previousStatus =
                    prevEnabled ? "enabled" : (prevRestricted ? "restricted" : "pending");
        }

        if (currentStatus.equals(previousStatus)) {
            return;
        }

        String accountId = account.getId();
        String accountType =
                stripeAccountWebhookRepository.findAccountTypeByStripeAccountId(accountId);

        switch (currentStatus) {

            case "enabled":
                if ("CLUB".equals(accountType)) {
                    stripeAccountWebhookRepository
                            .updateClubAccountStatus(accountId, "Enabled");
                } else if ("MEMBER".equals(accountType)) {
                    stripeAccountWebhookRepository
                            .updateMemberAccountStatus(accountId, "Enabled");
                }
                break;

            case "restricted":
                if ("CLUB".equals(accountType)) {
                    stripeAccountWebhookRepository
                            .updateClubAccountStatus(accountId, "Restricted");
                } else if ("MEMBER".equals(accountType)) {
                    stripeAccountWebhookRepository
                            .updateMemberAccountStatus(accountId, "Restricted");
                }
                break;

            default:
                if ("CLUB".equals(accountType)) {
                    stripeAccountWebhookRepository
                            .updateClubAccountStatus(accountId, "Pending");
                } else if ("MEMBER".equals(accountType)) {
                    stripeAccountWebhookRepository
                            .updateMemberAccountStatus(accountId, "Pending");
                }
        }
    }
}
