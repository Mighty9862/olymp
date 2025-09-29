package org.example.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class OlympiadResponse {
    private String name;
    private LocalDate date;
    private String description; // Добавлено новое поле
}