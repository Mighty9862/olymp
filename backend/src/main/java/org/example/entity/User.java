package org.example.entity;

import org.example.enums.Gender;
import org.example.enums.Role;  // Добавь импорт
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.ColumnTransformer;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Email
    private String email;

    @Column(nullable = false)
    private String password;  // Хэшированный BCrypt

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column
    private String lastName;  // Encrypted

    @Column
    private String firstName; // Encrypted

    @Column
    private String middleName; // Encrypted

    @Column
    @NotNull
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Gender gender;

    @Column
    private String classCourse;

    @Column
    private String educationalInstitution;

    @Column
    private String institutionAddress; // Encrypted

    @Column
    private String phoneNumber; // Encrypted

    @Column
    private String residenceRegion; // Encrypted

    @Column
    private String residenceSettlement; // Encrypted

    @Column
    private String snils; // Encrypted

    @Column
    private String postalAddress; // Encrypted

    @Column(nullable = false)
    private LocalDate registrationDate;

    @PrePersist
    protected void onCreate() {
        registrationDate = LocalDate.now();
    }

    @ManyToMany
    @JoinTable(
            name = "user_olympiads",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "olympiad_id")
    )
    private Set<Olympiad> olympiads = new HashSet<>();
}