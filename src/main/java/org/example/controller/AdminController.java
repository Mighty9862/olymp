package org.example.controller;

import org.example.enums.Role;
import org.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Endpoints for admin role management")
@PreAuthorize("hasRole('ADMIN')")  // Только для админов
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/assign/{email}")
    public ResponseEntity<String> assignAdmin(@PathVariable String email) {
        userService.setRole(email, Role.ADMIN);
        return ResponseEntity.ok("ADMIN role assigned to " + email);
    }

    @PostMapping("/remove/{email}")
    public ResponseEntity<String> removeAdmin(@PathVariable String email) {
        userService.setRole(email, Role.USER);
        return ResponseEntity.ok("ADMIN role removed from " + email);
    }
}