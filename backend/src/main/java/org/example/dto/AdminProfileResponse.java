package org.example.dto;

import org.example.enums.Gender;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AdminProfileResponse {
    private String email;
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
    
    // Добавляем поле для выбранных олимпиад (только для информации)
    private List<OlympiadResponse> selectedOlympiads;
}