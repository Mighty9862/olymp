package org.example.controller;

import org.example.dto.CreateNewsRequest;
import org.example.dto.NewsResponse;
import org.example.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/news")
@Tag(name = "Admin News", description = "Endpoints for admin news management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNewsController {
    private final NewsService newsService;

    public AdminNewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping
    @Operation(summary = "Create news", description = "Create a new news item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "News created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<NewsResponse> createNews(@Valid @RequestBody CreateNewsRequest request) {
        NewsResponse response = newsService.createNews(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete news", description = "Delete news by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "News deleted"),
            @ApiResponse(responseCode = "404", description = "News not found")
    })
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }
}