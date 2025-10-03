package org.example.controller;

import org.example.dto.ProfileResponse;
import org.example.dto.OlympiadResponse;
import org.example.enums.Role;
import org.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Endpoints for admin role management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/assign/{email}")
    public ResponseEntity<String> assignAdmin(@PathVariable String email) {
        userService.setRole(email, Role.ADMIN);
        return ResponseEntity.ok("ADMIN role assigned to " + email);
    }

    @PostMapping("/remove/{email}")
    public ResponseEntity<String> removeAdmin(@PathVariable String email) {
        userService.setRole(email, Role.USER);
        return ResponseEntity.ok("ADMIN role removed from " + email);
    }

    @GetMapping("/export-users")
    @Operation(summary = "Export user data to Excel", description = "Download Excel file with user data in specified format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel file downloaded")
    })
    public ResponseEntity<ByteArrayResource> exportUsers() throws IOException {
        List<ProfileResponse> users = userService.getAllUserProfiles();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        // Стили для заголовков
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Создание заголовков согласно формату (убрали ненужные поля)
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Дата", "№", "Фамилия", "Имя", "Отчество", "Дата рождения", "Пол",
                "СНИЛС", "Место жительства", "Номер телефона", "e-mail",
                "Регион образовательной организации", "Наименование образовательной организации",
                "Класс/Курс", "Логин", "Пароль", "Выбранные олимпиады"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Форматтер для дат
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        DateTimeFormatter birthDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        // Заполнение данных
        int rowNum = 1;
        for (ProfileResponse user : users) {
            Row row = sheet.createRow(rowNum++);

            // Дата регистрации
            String registrationDateStr = user.getRegistrationDate() != null ? 
                user.getRegistrationDate().format(dateFormatter) : LocalDate.now().format(dateFormatter);
            row.createCell(0).setCellValue(registrationDateStr);

            // Порядковый номер
            row.createCell(1).setCellValue(rowNum - 1);

            // Фамилия, Имя, Отчество
            row.createCell(2).setCellValue(user.getLastName() != null ? user.getLastName() : "");
            row.createCell(3).setCellValue(user.getFirstName() != null ? user.getFirstName() : "");
            row.createCell(4).setCellValue(user.getMiddleName() != null ? user.getMiddleName() : "");

            // Дата рождения
            String birthDateStr = user.getBirthDate() != null ? user.getBirthDate().format(birthDateFormatter) : "";
            row.createCell(5).setCellValue(birthDateStr);

            // Пол (преобразуем в "м"/"ж")
            String genderStr = "";
            if (user.getGender() != null) {
                switch (user.getGender()) {
                    case MALE -> genderStr = "м";
                    case FEMALE -> genderStr = "ж";
                }
            }
            row.createCell(6).setCellValue(genderStr);

            // СНИЛС
            row.createCell(7).setCellValue(user.getSnils() != null ? user.getSnils() : "");

            // Место жительства (объединяем регион и населенный пункт)
            String residence = "";
            if (user.getResidenceRegion() != null && user.getResidenceSettlement() != null) {
                residence = user.getResidenceRegion() + ", " + user.getResidenceSettlement();
            } else if (user.getResidenceRegion() != null) {
                residence = user.getResidenceRegion();
            } else if (user.getResidenceSettlement() != null) {
                residence = user.getResidenceSettlement();
            }
            row.createCell(8).setCellValue(residence);

            // Номер телефона
            row.createCell(9).setCellValue(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");

            // Email
            row.createCell(10).setCellValue(user.getEmail() != null ? user.getEmail() : "");

            // Регион образовательной организации
            row.createCell(11).setCellValue(user.getResidenceRegion() != null ? user.getResidenceRegion() : "");

            // Наименование образовательной организации
            row.createCell(12).setCellValue(user.getEducationalInstitution() != null ? user.getEducationalInstitution() : "");

            // Класс/Курс
            row.createCell(13).setCellValue(user.getClassCourse() != null ? user.getClassCourse() : "");

            // Логин (используем email)
            row.createCell(14).setCellValue(user.getEmail() != null ? user.getEmail() : "");

            // Пароль (ставим звездочку *)
            row.createCell(15).setCellValue("*");

            // Выбранные олимпиады
            if (user.getSelectedOlympiads() != null && !user.getSelectedOlympiads().isEmpty()) {
                StringBuilder olympiadInfo = new StringBuilder();
                for (OlympiadResponse olympiad : user.getSelectedOlympiads()) {
                    olympiadInfo.append(olympiad.getName());
                    if (olympiad.getDate() != null) {
                        olympiadInfo.append(" (").append(olympiad.getDate().format(dateFormatter)).append(")");
                    }
                    olympiadInfo.append("; ");
                }
                if (olympiadInfo.length() > 2) {
                    olympiadInfo.setLength(olympiadInfo.length() - 2); // Удаляем последний "; "
                }
                row.createCell(16).setCellValue(olympiadInfo.toString());
            } else {
                row.createCell(16).setCellValue("");
            }
        }

        // Авто-размер колонок
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Запись в byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        byte[] bytes = baos.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_export_" + LocalDate.now() + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new ByteArrayResource(bytes));
    }
}