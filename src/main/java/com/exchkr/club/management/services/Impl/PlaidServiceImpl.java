package com.exchkr.club.management.services.Impl;

import com.exchkr.club.management.dao.PlaidAccountRepository;
import com.exchkr.club.management.model.api.request.PlaidExchangeRequest;
import com.exchkr.club.management.model.entity.PlaidAccount;
import com.exchkr.club.management.services.PlaidService;
import com.plaid.client.ApiClient;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import retrofit2.Response;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class PlaidServiceImpl implements PlaidService {

	private final PlaidApi plaidClient;
	private final PlaidAccountRepository plaidAccountRepository;
	private final TextEncryptor encryptor;
	private final String plaidWebhookUrl;

	public PlaidServiceImpl(PlaidAccountRepository plaidAccountRepository, @Value("${plaid.clientID}") String clientID,
			@Value("${plaid.secret}") String secret, @Value("${plaid.env}") String env,
			@Value("${plaid.webhookUrl}") String plaidWebhookUrl, @Value("${encryption.password}") String encPassword,
			@Value("${encryption.salt}") String encSalt) {
		this.plaidAccountRepository = plaidAccountRepository;
		this.encryptor = Encryptors.text(encPassword, encSalt);
		this.plaidWebhookUrl = plaidWebhookUrl;

		Map<String, String> apiKeys = new HashMap<>();
		apiKeys.put("clientId", clientID);
		apiKeys.put("secret", secret);

		ApiClient apiClient = new ApiClient(apiKeys);
		String baseUrl = "production".equalsIgnoreCase(env) ? "https://production.plaid.com"
				: "development".equalsIgnoreCase(env) ? "https://development.plaid.com" : "https://sandbox.plaid.com";

		apiClient.setPlaidAdapter(baseUrl);
		this.plaidClient = apiClient.createService(PlaidApi.class);
	}

	@Override
	public Map<String, Object> getLinkStatus(Long clubId) {
		// Find ANY account record for this club
		Optional<PlaidAccount> plaidAccountOpt = plaidAccountRepository.findByClubId(clubId);
		Map<String, Object> response = new HashMap<>();

		if (plaidAccountOpt.isPresent()) {
			PlaidAccount account = plaidAccountOpt.get();
			response.put("isLinked", true);
			response.put("active", account.isActiveInd()); // This tells frontend if it's broken
			response.put("institutionName", account.getInstitutionName());
			response.put("defaultAccountId", account.getDefaultBankAccount());

			// If activeInd is false but accessToken exists, it's a "Repair" state
			response.put("needsRepair", !account.isActiveInd() && account.getAccessToken() != null);
		} else {
			response.put("isLinked", false);
			response.put("needsRepair", false);
		}
		return response;
	}

	@Override
	public String createLinkToken(Long userId, Long clubId) throws Exception {
	    Optional<PlaidAccount> existingAccount = plaidAccountRepository.findByClubId(clubId);

//	    System.out.println("DEBUG: Account present for club " + clubId + ": " + existingAccount.isPresent());

	    LinkTokenCreateRequestUser user = new LinkTokenCreateRequestUser().clientUserId(userId.toString());

	    LinkTokenCreateRequest request = new LinkTokenCreateRequest()
	            .user(user)
	            .clientName("Exchkr Club")
	            .countryCodes(List.of(CountryCode.US))
	            .language("en");

	    // 2. Separate the flows strictly
	    if (existingAccount.isPresent() && existingAccount.get().getAccessToken() != null
	            && !existingAccount.get().getAccessToken().isEmpty()) {

	        // UPDATE MODE
	        request.accessToken(encryptor.decrypt(existingAccount.get().getAccessToken()));
	        // Note: We do NOT set webhooks or products here
	    } else {
	        // INITIAL LINK MODE
	        request.products(List.of(Products.TRANSACTIONS));
	        
	        // Only add webhook if it is actually present/valid
	        if (plaidWebhookUrl != null && !plaidWebhookUrl.isBlank()) {
	            request.webhook(plaidWebhookUrl);
	        }
	    }

	    Response<LinkTokenCreateResponse> response = plaidClient.linkTokenCreate(request).execute();

//	    if (!response.isSuccessful()) {
//	        // Log the actual error body to console so we can see what Plaid is hating
//	        String errorDetail = response.errorBody().string();
//	        System.err.println("PLAID ERROR DETAIL: " + errorDetail);
//	        throw new IOException("Plaid Link Error: " + errorDetail);
//	    }

	    return response.body().getLinkToken();
	}

	@Override
	@Transactional
	public void reactivateAccount(Long clubId) {
		plaidAccountRepository.findByClubId(clubId).ifPresent(account -> {
			account.setActiveInd(true);
			plaidAccountRepository.save(account);
		});
	}

	@Override
	@Transactional
	public String exchangePublicToken(PlaidExchangeRequest request, Long userId, Long clubId) throws Exception {
		// Deactivate old links for this club
		plaidAccountRepository.findByClubIdAndActiveIndTrue(clubId).ifPresent(old -> {
			old.setActiveInd(false);
			old.setUpdatedAt(OffsetDateTime.now());
			plaidAccountRepository.save(old);
		});

		ItemPublicTokenExchangeRequest exchangeRequest = new ItemPublicTokenExchangeRequest()
				.publicToken(request.getPublicToken());
		Response<ItemPublicTokenExchangeResponse> response = plaidClient.itemPublicTokenExchange(exchangeRequest)
				.execute();

		PlaidAccount newAccount = new PlaidAccount();
		newAccount.setId(UUID.randomUUID());
		newAccount.setClubId(clubId);
		newAccount.setCreatedByUserId(userId);
		newAccount.setItemId(response.body().getItemId());
		newAccount.setAccessToken(encryptor.encrypt(response.body().getAccessToken()));
		newAccount.setInstitutionId(request.getInstitutionId());
		newAccount.setInstitutionName(request.getInstitutionName());
		newAccount.setAccountIds(request.getAccountIds());
		newAccount.setDefaultBankAccount(request.getAccountIds().get(0));
		newAccount.setActiveInd(true);
		newAccount.setCreatedAt(OffsetDateTime.now());
		newAccount.setUpdatedAt(OffsetDateTime.now());

		plaidAccountRepository.save(newAccount);
		return "Bank account linked successfully.";
	}

	@Override
	public List<AccountBase> getAccountBalance(Long clubId) throws Exception {
		PlaidAccount account = plaidAccountRepository.findByClubIdAndActiveIndTrue(clubId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No linked bank found."));

		AccountsBalanceGetRequest request = new AccountsBalanceGetRequest()
				.accessToken(encryptor.decrypt(account.getAccessToken()));

		Response<AccountsGetResponse> response = plaidClient.accountsBalanceGet(request).execute();
		if (!response.isSuccessful())
			handlePlaidError(response, account);

		return response.body().getAccounts();
	}

	@Override
	@Transactional
	public void updateDefaultAccount(Long clubId, String newAccountId) {
		PlaidAccount account = plaidAccountRepository.findByClubIdAndActiveIndTrue(clubId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No linked bank found"));

		if (!account.getAccountIds().contains(newAccountId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Account ID for this club");
		}

		account.setDefaultBankAccount(newAccountId);
		account.setUpdatedAt(OffsetDateTime.now());
		plaidAccountRepository.save(account);
	}

	@Override
	@Transactional
	public void unlinkAccount(Long clubId) throws Exception {
		PlaidAccount account = plaidAccountRepository.findByClubIdAndActiveIndTrue(clubId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active link found"));

		// 1. Tell Plaid to kill the token permanently
		ItemRemoveRequest removeRequest = new ItemRemoveRequest()
				.accessToken(encryptor.decrypt(account.getAccessToken()));
		plaidClient.itemRemove(removeRequest).execute();

		plaidAccountRepository.delete(account);
	}

	private void handlePlaidError(Response<?> response, PlaidAccount account) throws IOException {
		String errorBody = response.errorBody().string();
		if (errorBody.contains("ITEM_LOGIN_REQUIRED")) {
			account.setActiveInd(false);
			plaidAccountRepository.save(account);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bank connection expired. Please reconnect.");
		}
		throw new IOException("Plaid API Error: " + errorBody);
	}
}