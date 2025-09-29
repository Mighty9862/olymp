package org.example.controller;

import org.example.dto.OlympiadResponse;
import org.example.entity.Olympiad;
import org.example.service.OlympiadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/olympiads")
@Tag(name = "Olympiads", description = "Endpoints for managing available olympiads")
public class OlympiadController {
    private final OlympiadService olympiadService;

    public OlympiadController(OlympiadService olympiadService) {
        this.olympiadService = olympiadService;
    }

    @GetMapping
    @Operation(summary = "Get all available olympiads", description = "Retrieve list of all olympiads with names, dates and descriptions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Olympiads retrieved")
    })
    public ResponseEntity<List<OlympiadResponse>> getAllOlympiads() {
        List<OlympiadResponse> responses = olympiadService.getAll().stream().map(o -> {
            OlympiadResponse r = new OlympiadResponse();
            r.setName(o.getName());
            r.setDate(o.getDate());
            r.setDescription(o.getDescription()); // Добавлено описание
            return r;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}