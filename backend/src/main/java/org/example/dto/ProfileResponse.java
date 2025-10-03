package org.example.dto;

import org.example.enums.Gender;
import org.example.enums.Role;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProfileResponse {
    private Long id;
    private String email;
    private Role role;

    // Дешифрованные персональные данные
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

    private LocalDate registrationDate;

    private List<OlympiadResponse> selectedOlympiads;
}