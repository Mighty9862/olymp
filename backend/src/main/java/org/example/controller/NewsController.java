package org.example.controller;

import org.example.dto.NewsResponse;
import org.example.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@Tag(name = "News", description = "Endpoints for news")
public class NewsController {
    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    @Operation(summary = "Get all news", description = "Retrieve all news sorted by date (newest first)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "News retrieved")
    })
    public ResponseEntity<List<NewsResponse>> getAllNews() {
        List<NewsResponse> news = newsService.getAllNews();
        return ResponseEntity.ok(news);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get news by ID", description = "Retrieve specific news by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "News found"),
            @ApiResponse(responseCode = "404", description = "News not found")
    })
    public ResponseEntity<NewsResponse> getNewsById(@PathVariable Long id) {
        NewsResponse news = newsService.getNewsById(id);
        return ResponseEntity.ok(news);
    }
}