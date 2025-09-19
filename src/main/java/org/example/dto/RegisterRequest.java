package org.example.dto;

import org.example.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String lastName;

    @NotBlank
    private String firstName;

    private String middleName;

    @NotNull
    private LocalDate birthDate;

    @NotNull
    private Gender gender;

    private String classCourse;

    @NotBlank
    private String educationalInstitution;

    @NotBlank
    private String institutionAddress;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String residenceRegion;

    @NotBlank
    private String residenceSettlement;

    @NotBlank
    private String snils;

    @NotBlank
    private String postalAddress;
}