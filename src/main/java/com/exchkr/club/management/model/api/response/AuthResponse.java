package com.exchkr.club.management.model.api.response;

public class AuthResponse {

    private String message;

    // Required by Jackson
    public AuthResponse() {}



    public AuthResponse(String message) {
        this.message = message;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
