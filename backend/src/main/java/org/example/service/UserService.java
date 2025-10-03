// src/main/java/org/example/service/UserService.java
// (Updated with new method getAllUserProfiles. Replace the entire file with this.)
package org.example.service;

import org.example.dto.OlympiadResponse;
import org.example.dto.ProfileResponse;
import org.example.dto.ProfileUpdateRequest;
import org.example.dto.RegisterRequest;
import org.example.entity.Olympiad;
import org.example.entity.User;
import org.example.enums.Role;
import org.example.exception.EmailExistsException;
import org.example.exception.OlympiadNotFoundException;
import org.example.exception.UserNotFoundException;
import org.example.repository.UserRepository;
import org.example.util.EncryptionUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final OlympiadService olympiadService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, EncryptionUtil encryptionUtil, OlympiadService olympiadService) {
        this.userRepository = userRepository;
        this.encryptionUtil = encryptionUtil;
        this.olympiadService = olympiadService;
    }

    public User register(RegisterRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new EmailExistsException("Email already exists: " + request.getEmail());  // Кастомное исключение
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Первый пользователь — ADMIN, остальные — USER
        if (userRepository.count() == 0) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.USER);
        }

        // Шифруем персональные данные
        user.setLastName(encryptionUtil.encrypt(request.getLastName()));
        user.setFirstName(encryptionUtil.encrypt(request.getFirstName()));
        if (request.getMiddleName() != null) {
            user.setMiddleName(encryptionUtil.encrypt(request.getMiddleName()));
        }
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());
        user.setClassCourse(request.getClassCourse());
        user.setEducationalInstitution(request.getEducationalInstitution());
        user.setInstitutionAddress(encryptionUtil.encrypt(request.getInstitutionAddress()));
        user.setPhoneNumber(encryptionUtil.encrypt(request.getPhoneNumber()));
        user.setResidenceRegion(encryptionUtil.encrypt(request.getResidenceRegion()));
        user.setResidenceSettlement(encryptionUtil.encrypt(request.getResidenceSettlement()));
        // Устанавливаем значение по умолчанию, если тип населенного пункта не указан
        user.setSettlementType(request.getSettlementType() != null ? request.getSettlementType() : "Не указано");
        user.setSnils(encryptionUtil.encrypt(request.getSnils()));
        user.setPostalAddress(encryptionUtil.encrypt(request.getPostalAddress()));

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));  // Кастомное
    }

    public void setRole(String email, Role role) {
        User user = findByEmail(email);
        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public ProfileResponse getProfileByEmail(String email) {
        User user = findByEmail(email);

        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        // Дешифруем персональные данные
        response.setLastName(encryptionUtil.decrypt(user.getLastName()));
        response.setFirstName(encryptionUtil.decrypt(user.getFirstName()));
        if (user.getMiddleName() != null) {
            response.setMiddleName(encryptionUtil.decrypt(user.getMiddleName()));
        }
        response.setBirthDate(user.getBirthDate());
        response.setGender(user.getGender());
        response.setClassCourse(user.getClassCourse());
        response.setEducationalInstitution(user.getEducationalInstitution());
        response.setInstitutionAddress(encryptionUtil.decrypt(user.getInstitutionAddress()));
        response.setPhoneNumber(encryptionUtil.decrypt(user.getPhoneNumber()));
        response.setResidenceRegion(encryptionUtil.decrypt(user.getResidenceRegion()));
        response.setResidenceSettlement(encryptionUtil.decrypt(user.getResidenceSettlement()));
        response.setSnils(encryptionUtil.decrypt(user.getSnils()));
        response.setPostalAddress(encryptionUtil.decrypt(user.getPostalAddress()));
        response.setRegistrationDate(user.getRegistrationDate());

        response.setSelectedOlympiads(user.getOlympiads().stream().map(o -> {
            OlympiadResponse r = new OlympiadResponse();
            r.setName(o.getName());
            r.setDate(o.getDate());
            return r;
        }).collect(Collectors.toList()));

        return response;
    }

    // Обновление профиля (частичное)
    public User updateProfile(String currentEmail, ProfileUpdateRequest request) {
        User user = findByEmail(currentEmail);  // Текущий пользователь

        // Проверяем уникальность email, если изменён
        if (request.getEmail() != null && !request.getEmail().equals(currentEmail)) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new EmailExistsException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Обновляем только переданные поля (с шифрованием персональных)
        if (request.getLastName() != null) user.setLastName(encryptionUtil.encrypt(request.getLastName()));
        if (request.getFirstName() != null) user.setFirstName(encryptionUtil.encrypt(request.getFirstName()));
        if (request.getMiddleName() != null) user.setMiddleName(encryptionUtil.encrypt(request.getMiddleName()));
        if (request.getBirthDate() != null) user.setBirthDate(request.getBirthDate());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getClassCourse() != null) user.setClassCourse(request.getClassCourse());
        if (request.getEducationalInstitution() != null) user.setEducationalInstitution(request.getEducationalInstitution());
        if (request.getInstitutionAddress() != null) user.setInstitutionAddress(encryptionUtil.encrypt(request.getInstitutionAddress()));
        if (request.getPhoneNumber() != null) user.setPhoneNumber(encryptionUtil.encrypt(request.getPhoneNumber()));
        if (request.getResidenceRegion() != null) user.setResidenceRegion(encryptionUtil.encrypt(request.getResidenceRegion()));
        if (request.getResidenceSettlement() != null) user.setResidenceSettlement(encryptionUtil.encrypt(request.getResidenceSettlement()));
        if (request.getSnils() != null) user.setSnils(encryptionUtil.encrypt(request.getSnils()));
        if (request.getPostalAddress() != null) user.setPostalAddress(encryptionUtil.encrypt(request.getPostalAddress()));

        return userRepository.save(user);
    }

    public void addOlympiads(String email, List<String> names) {
        User user = findByEmail(email);
        for (String name : names) {
            Optional<Olympiad> opt = olympiadService.findByName(name);
            if (opt.isEmpty()) {
                throw new OlympiadNotFoundException("Olympiad not found: " + name);
            }
            user.getOlympiads().add(opt.get());
        }
        userRepository.save(user);
    }

    public void removeOlympiad(String email, String name) {
        User user = findByEmail(email);
        Optional<Olympiad> opt = olympiadService.findByName(name);
        if (opt.isEmpty()) {
            throw new OlympiadNotFoundException("Olympiad not found: " + name);
        }
        user.getOlympiads().remove(opt.get());
        userRepository.save(user);
    }

    public List<ProfileResponse> getAllUserProfiles() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.USER)
                .map(u -> getProfileByEmail(u.getEmail()))
                .collect(Collectors.toList());
    }
}