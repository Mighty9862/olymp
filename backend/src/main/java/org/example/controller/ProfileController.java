package org.example.controller;

import org.example.dto.ProfileResponse;
import org.example.dto.ProfileUpdateRequest;
import org.example.entity.User;
import org.example.exception.UserNotFoundException;
import org.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "Endpoints for user profile management")
@PreAuthorize("isAuthenticated()")
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get current user profile", description = "Retrieve decrypted profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        String email = authentication.getName();
        ProfileResponse profile = userService.getProfileByEmail(email);
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    @Operation(summary = "Update current user profile", description = "Update profile data (partial update)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation error or email exists"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ProfileResponse> updateProfile(Authentication authentication,
                                                         @Valid @RequestBody ProfileUpdateRequest request) {
        String email = authentication.getName();
        User updatedUser = userService.updateProfile(email, request);
        ProfileResponse profile = userService.getProfileByEmail(updatedUser.getEmail());
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/olympiads/select")
    @Operation(summary = "Select olympiads", description = "Assign olympiads to current user (array of olympiad names)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Olympiads selected"),
            @ApiResponse(responseCode = "404", description = "Olympiad not found")
    })
    public ResponseEntity<String> selectOlympiads(Authentication authentication, @RequestBody List<String> olympiadNames) {
        String email = authentication.getName();
        userService.addOlympiads(email, olympiadNames);
        return ResponseEntity.ok("Olympiads selected successfully");
    }

    @DeleteMapping("/olympiads/{name}")
    @Operation(summary = "Remove olympiad", description = "Remove selected olympiad from current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Olympiad removed"),
            @ApiResponse(responseCode = "404", description = "Olympiad not found")
    })
    public ResponseEntity<String> removeOlympiad(Authentication authentication, @PathVariable String name) {
        String email = authentication.getName();
        userService.removeOlympiad(email, name);
        return ResponseEntity.ok("Olympiad removed successfully");
    }
}