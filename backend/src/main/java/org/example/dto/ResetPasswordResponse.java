package org.example.dto;

import lombok.Data;

@Data
public class ResetPasswordResponse {
    private String message;
    private boolean success;

    public ResetPasswordResponse() {}

    public ResetPasswordResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }
}