package org.example.controller;

import org.example.dto.*;
import org.example.entity.User;
import org.example.service.PasswordResetService;
import org.example.service.UserService;
import org.example.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, logout and password reset")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService, JwtUtil jwtUtil, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account and automatically login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered and logged in successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email exists")
    })
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setMessage("Registration successful and automatically logged in");
        response.setRole(user.getRole());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token with user role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByEmail(request.getEmail());
        if (!userService.validatePassword(request.getPassword(), user.getPassword())) {
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setMessage("Invalid credentials");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setMessage("Login successful");
        response.setRole(user.getRole());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidate session (client-side)")
    @PreAuthorize("isAuthenticated()")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully")
    })
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Send password reset link to email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset link sent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok("Ссылка для восстановления пароля отправлена на ваш email");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using reset token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        ResetPasswordResponse response = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-reset-token/{token}")
    @Operation(summary = "Validate reset token", description = "Check if password reset token is valid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "400", description = "Token is invalid or expired")
    })
    public ResponseEntity<ResetPasswordResponse> validateResetToken(@PathVariable String token) {
        boolean isValid = passwordResetService.validateToken(token);
        if (isValid) {
            return ResponseEntity.ok(new ResetPasswordResponse("Токен действителен", true));
        } else {
            return ResponseEntity.badRequest().body(new ResetPasswordResponse("Неверный или просроченный токен", false));
        }
    }
}