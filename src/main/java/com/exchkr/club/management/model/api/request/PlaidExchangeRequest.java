package com.exchkr.club.management.model.api.request;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlaidExchangeRequest {
	
	@JsonProperty("public_token")
    private String publicToken;
	
	@JsonProperty("institution_id")
    private String institutionId;
	
	@JsonProperty("institution_name")
    private String institutionName;
	
	@JsonProperty("account_ids")
    private List<String> accountIds;
	
	
	
	public String getPublicToken() {
		return publicToken;
	}
	public void setPublicToken(String publicToken) {
		this.publicToken = publicToken;
	}
	public String getInstitutionId() {
		return institutionId;
	}
	public void setInstitutionId(String institutionId) {
		this.institutionId = institutionId;
	}
	public String getInstitutionName() {
		return institutionName;
	}
	public void setInstitutionName(String institutionName) {
		this.institutionName = institutionName;
	}
	public List<String> getAccountIds() {
		return accountIds;
	}
	public void setAccountIds(List<String> accountIds) {
		this.accountIds = accountIds;
	} 


}