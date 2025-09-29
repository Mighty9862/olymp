package org.example.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NewsResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate newsDate;
    private LocalDate createdAt;
}