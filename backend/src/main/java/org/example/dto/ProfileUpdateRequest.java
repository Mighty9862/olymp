package org.example.dto;

import org.example.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequest {
    private String email;  // Опционально, но уникально

    // Опциональные персональные (валидация только если переданы)
    private String lastName;
    private String firstName;
    private String middleName;
    private LocalDate birthDate;
    private Gender gender;
    private String classCourse;
    private String educationalInstitution;
    private String institutionAddress;
    private String phoneNumber;
    private String residenceRegion;
    private String residenceSettlement;
    private String snils;
    private String postalAddress;
}