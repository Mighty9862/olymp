package org.example.dto;

import lombok.Data;
import org.example.enums.Role;

@Data
public class LoginResponse {
    private String token;
    private String message;
    private Role role; // Добавлено поле роли

    // Конструктор по умолчанию для случаев ошибок
    public LoginResponse() {
    }

    public LoginResponse(String token, String message, Role role) {
        this.token = token;
        this.message = message;
        this.role = role;
    }
}