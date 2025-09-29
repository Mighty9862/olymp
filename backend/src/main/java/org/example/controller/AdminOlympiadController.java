package org.example.controller;

import org.example.dto.AddOlympiadRequest;
import org.example.entity.Olympiad;
import org.example.service.OlympiadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/olympiads")
@Tag(name = "Admin Olympiads", description = "Endpoints for admin olympiad management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOlympiadController {
    private final OlympiadService olympiadService;

    public AdminOlympiadController(OlympiadService olympiadService) {
        this.olympiadService = olympiadService;
    }

    @PostMapping
    @Operation(summary = "Add new olympiad", description = "Create a new olympiad by name, date and description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Olympiad added"),
            @ApiResponse(responseCode = "400", description = "Olympiad already exists")
    })
    public ResponseEntity<Olympiad> addOlympiad(@RequestBody AddOlympiadRequest request) {
        Olympiad olympiad = olympiadService.create(request.getName(), request.getDate(), request.getDescription());
        return ResponseEntity.ok(olympiad);
    }

    @DeleteMapping("/{name}")
    @Operation(summary = "Delete olympiad", description = "Remove olympiad by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Olympiad deleted"),
            @ApiResponse(responseCode = "404", description = "Olympiad not found")
    })
    public ResponseEntity<Void> deleteOlympiad(@PathVariable String name) {
        olympiadService.deleteByName(name);
        return ResponseEntity.noContent().build();
    }
}