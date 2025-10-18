package org.example.controller;

import org.example.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/carousel")
@Tag(name = "Admin Carousel", description = "Endpoints for admin carousel image management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCarouselController {
    private final ImageService imageService;

    public AdminCarouselController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload images for carousel", description = "Upload one or multiple images")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Images uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid file")
    })
    public ResponseEntity<List<String>> uploadImages(
            @Parameter(description = "Files to upload", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary")))
            @RequestPart("files") List<MultipartFile> files) throws IOException {
        List<String> urls = imageService.uploadImages(files);
        return ResponseEntity.ok(urls);
    }

    @DeleteMapping("/{filename:.+}")
    @Operation(summary = "Delete carousel image", description = "Delete image by filename (only filename, not full path)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Image deleted"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "Filename only (e.g., c9f51d9b-e889-488c-a6b1-66f2b4f5b1ba.jpeg)", example = "c9f51d9b-e889-488c-a6b1-66f2b4f5b1ba.jpeg")
            @PathVariable String filename) throws IOException {
        imageService.deleteImage(filename);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/order")
    @Operation(summary = "Update image order", description = "Update the display order of carousel images")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order data")
    })
    public ResponseEntity<Void> updateImageOrder(
            @Parameter(description = "List of image URLs in new order")
            @RequestBody List<String> newOrder) throws IOException {
        imageService.updateImageOrder(newOrder);
        return ResponseEntity.ok().build();
    }
}