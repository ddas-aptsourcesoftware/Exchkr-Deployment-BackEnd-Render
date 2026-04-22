package com.exchkr.club.management.model.api.request;

public class ClubOnboardingRequest {
    private String clubName;
    private String fullName;
    private String email;
    private String schoolName;

    public ClubOnboardingRequest() {}

    public ClubOnboardingRequest(String clubName, String fullName, String email, String schoolName) {
        this.clubName = clubName;
        this.fullName = fullName;
        this.email = email;
        this.schoolName = schoolName;
    }

    public String getClubName() { return clubName; }
    
    public String getSchoolName() {
		return schoolName;
	}

	public void setSchoolName(String schoolName) {
		this.schoolName = schoolName;
	}

	public void setClubName(String clubName) { this.clubName = clubName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}