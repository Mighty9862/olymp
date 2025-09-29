package org.example.controller;

import org.example.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/carousel")
@Tag(name = "Carousel", description = "Endpoints for carousel images")
public class CarouselController {
    private final ImageService imageService;

    public CarouselController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/images")
    @Operation(summary = "Get all carousel image URLs", description = "Retrieve list of URLs for all uploaded images. Use only the filename (after last '/') for deletion.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Images retrieved")
    })
    public ResponseEntity<List<String>> getAllImages() throws IOException {
        List<String> urls = imageService.getAllImageUrls();
        return ResponseEntity.ok(urls);
    }
}