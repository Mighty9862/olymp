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
import java.util.*;
import java.util.stream.Collectors;

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

    @GetMapping("/export-users-simple")
    @Operation(summary = "Export user data to Excel with highlights", description = "Users without selected olympiads are highlighted in yellow, duplicates in blue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel file downloaded")
    })
    public ResponseEntity<ByteArrayResource> exportUsers() throws IOException {
        List<ProfileResponse> users = userService.getAllUserProfiles();

        // Находим дублирующиеся ФИО
        Map<String, List<ProfileResponse>> fioGroups = users.stream()
                .collect(Collectors.groupingBy(user -> 
                    (user.getLastName() != null ? user.getLastName() : "") + "|" +
                    (user.getFirstName() != null ? user.getFirstName() : "") + "|" +
                    (user.getMiddleName() != null ? user.getMiddleName() : "")
                ));

        Set<String> duplicateFios = fioGroups.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        // Стили
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle highlightStyle = workbook.createCellStyle();
        highlightStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        highlightStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle duplicateStyle = workbook.createCellStyle();
        duplicateStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        duplicateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Заголовки
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Дата", "№", "Фамилия", "Имя", "Отчество", "Дата рождения", "Пол",
                "СНИЛС", "Место жительства", "Тип населенного пункта", "Номер телефона", "e-mail",
                "Регион образовательной организации", "Наименование образовательной организации",
                "Класс/Курс", "Логин", "Пароль", "Выбранные олимпиады"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        DateTimeFormatter birthDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        int rowNum = 1;
        for (ProfileResponse user : users) {
            Row row = sheet.createRow(rowNum++);

            boolean hasOlympiads = user.getSelectedOlympiads() != null && !user.getSelectedOlympiads().isEmpty();
            
            // Проверяем, является ли запись дублирующейся
            String userFioKey = (user.getLastName() != null ? user.getLastName() : "") + "|" +
                               (user.getFirstName() != null ? user.getFirstName() : "") + "|" +
                               (user.getMiddleName() != null ? user.getMiddleName() : "");
            boolean isDuplicate = duplicateFios.contains(userFioKey);

            CellStyle rowStyle = null;
            if (isDuplicate) {
                rowStyle = duplicateStyle;
            } else if (!hasOlympiads) {
                rowStyle = highlightStyle;
            }

            // Дата регистрации
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(user.getRegistrationDate() != null ? user.getRegistrationDate().format(dateFormatter) : LocalDate.now().format(dateFormatter));
            if (rowStyle != null) cell0.setCellStyle(rowStyle);

            // Порядковый номер
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(rowNum - 1);
            if (rowStyle != null) cell1.setCellStyle(rowStyle);

            // ФИО
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(user.getLastName() != null ? user.getLastName() : "");
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(user.getFirstName() != null ? user.getFirstName() : "");
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(user.getMiddleName() != null ? user.getMiddleName() : "");
            if (rowStyle != null) { cell2.setCellStyle(rowStyle); cell3.setCellStyle(rowStyle); cell4.setCellStyle(rowStyle); }

            // Дата рождения
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(user.getBirthDate() != null ? user.getBirthDate().format(birthDateFormatter) : "");
            if (rowStyle != null) cell5.setCellStyle(rowStyle);

            // Пол
            Cell cell6 = row.createCell(6);
            String genderStr = "";
            if (user.getGender() != null) {
                switch (user.getGender()) {
                    case MALE -> genderStr = "м";
                    case FEMALE -> genderStr = "ж";
                }
            }
            cell6.setCellValue(genderStr);
            if (rowStyle != null) cell6.setCellStyle(rowStyle);

            // СНИЛС
            Cell cell7 = row.createCell(7);
            cell7.setCellValue(user.getSnils() != null ? user.getSnils() : "");
            if (rowStyle != null) cell7.setCellStyle(rowStyle);

            // Место жительства
            Cell cell8 = row.createCell(8);
            StringBuilder residenceBuilder = new StringBuilder();
            if (user.getResidenceRegion() != null) {
                residenceBuilder.append(user.getResidenceRegion());
                if (user.getResidenceSettlement() != null) {
                    if (residenceBuilder.length() > 0) residenceBuilder.append(", ");
                    residenceBuilder.append(user.getResidenceSettlement());
                }
            }
            cell8.setCellValue(residenceBuilder.toString());
            if (rowStyle != null) cell8.setCellStyle(rowStyle);

            // Тип населенного пункта
            Cell cell9 = row.createCell(9);
            cell9.setCellValue(user.getSettlementType() != null ? user.getSettlementType() : "");
            if (rowStyle != null) cell9.setCellStyle(rowStyle);

            // Телефон и e-mail
            Cell cell10 = row.createCell(10);
            cell10.setCellValue(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
            Cell cell11 = row.createCell(11);
            cell11.setCellValue(user.getEmail() != null ? user.getEmail() : "");
            if (rowStyle != null) { cell10.setCellStyle(rowStyle); cell11.setCellStyle(rowStyle); }

            // Регион образовательной организации
            Cell cell12 = row.createCell(12);
            cell12.setCellValue(user.getResidenceRegion() != null ? user.getResidenceRegion() : "");
            if (rowStyle != null) cell12.setCellStyle(rowStyle);

            // Наименование образовательной организации
            Cell cell13 = row.createCell(13);
            cell13.setCellValue(user.getEducationalInstitution() != null ? user.getEducationalInstitution() : "");
            if (rowStyle != null) cell13.setCellStyle(rowStyle);

            // Класс/Курс
            Cell cell14 = row.createCell(14);
            cell14.setCellValue(user.getClassCourse() != null ? user.getClassCourse() : "");
            if (rowStyle != null) cell14.setCellStyle(rowStyle);

            // Логин
            Cell cell15 = row.createCell(15);
            cell15.setCellValue(user.getEmail() != null ? user.getEmail() : "");
            if (rowStyle != null) cell15.setCellStyle(rowStyle);

            // Пароль
            Cell cell16 = row.createCell(16);
            cell16.setCellValue("*");
            if (rowStyle != null) cell16.setCellStyle(rowStyle);

            // Выбранные олимпиады
            Cell cell17 = row.createCell(17);
            StringBuilder olympiadInfo = new StringBuilder();
            if (hasOlympiads) {
                for (OlympiadResponse olympiad : user.getSelectedOlympiads()) {
                    olympiadInfo.append(olympiad.getName());
                    if (olympiad.getDate() != null) {
                        olympiadInfo.append(" (").append(olympiad.getDate().format(dateFormatter)).append(")");
                    }
                    olympiadInfo.append("; ");
                }
                if (olympiadInfo.length() > 2) olympiadInfo.setLength(olympiadInfo.length() - 2);
            }
            cell17.setCellValue(olympiadInfo.toString());
            if (rowStyle != null) cell17.setCellStyle(rowStyle);
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        byte[] bytes = baos.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_export_" + LocalDate.now() + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new ByteArrayResource(bytes));
    }

    @GetMapping("/export-users")
    @Operation(summary = "Export simplified user data to Excel with highlights", description = "Users without selected olympiads highlighted in yellow, duplicates in blue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel file downloaded")
    })
    public ResponseEntity<ByteArrayResource> exportUsersSimple() throws IOException {
        List<ProfileResponse> users = userService.getAllUserProfiles();

        // Находим дублирующиеся ФИО
        Map<String, List<ProfileResponse>> fioGroups = users.stream()
                .collect(Collectors.groupingBy(user -> 
                    (user.getLastName() != null ? user.getLastName() : "") + "|" +
                    (user.getFirstName() != null ? user.getFirstName() : "") + "|" +
                    (user.getMiddleName() != null ? user.getMiddleName() : "")
                ));

        Set<String> duplicateFios = fioGroups.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Simple Users");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle highlightStyle = workbook.createCellStyle();
        highlightStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        highlightStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle duplicateStyle = workbook.createCellStyle();
        duplicateStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        duplicateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        String[] headers = {"Дата", "№", "Фамилия", "Имя", "Отчество", "Телефон", "e-mail", "Класс/Курс", "Выбранная Олимпиада"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        int rowNum = 1;
        for (ProfileResponse user : users) {
            Row row = sheet.createRow(rowNum++);
            boolean hasOlympiads = user.getSelectedOlympiads() != null && !user.getSelectedOlympiads().isEmpty();
            
            // Проверяем, является ли запись дублирующейся
            String userFioKey = (user.getLastName() != null ? user.getLastName() : "") + "|" +
                               (user.getFirstName() != null ? user.getFirstName() : "") + "|" +
                               (user.getMiddleName() != null ? user.getMiddleName() : "");
            boolean isDuplicate = duplicateFios.contains(userFioKey);

            CellStyle rowStyle = null;
            if (isDuplicate) {
                rowStyle = duplicateStyle;
            } else if (!hasOlympiads) {
                rowStyle = highlightStyle;
            }

            // Дата
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(user.getRegistrationDate() != null ? user.getRegistrationDate().format(dateFormatter) : LocalDate.now().format(dateFormatter));
            if (rowStyle != null) cell0.setCellStyle(rowStyle);

            // №
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(rowNum - 1);
            if (rowStyle != null) cell1.setCellStyle(rowStyle);

            // ФИО
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(user.getLastName() != null ? user.getLastName() : "");
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(user.getFirstName() != null ? user.getFirstName() : "");
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(user.getMiddleName() != null ? user.getMiddleName() : "");
            if (rowStyle != null) {
                cell2.setCellStyle(rowStyle);
                cell3.setCellStyle(rowStyle);
                cell4.setCellStyle(rowStyle);
            }

            // Телефон и e-mail
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(user.getEmail() != null ? user.getEmail() : "");
            if (rowStyle != null) {
                cell5.setCellStyle(rowStyle);
                cell6.setCellStyle(rowStyle);
            }

            // Класс/курс
            Cell cell7 = row.createCell(7);
            cell7.setCellValue(user.getClassCourse() != null ? user.getClassCourse() : "");
            if (rowStyle != null) cell7.setCellStyle(rowStyle);

            // Выбранная Олимпиада
            Cell cell8 = row.createCell(8);
            StringBuilder olympiads = new StringBuilder();
            if (hasOlympiads) {
                for (OlympiadResponse olympiad : user.getSelectedOlympiads()) {
                    olympiads.append(olympiad.getName());
                    if (olympiad.getDate() != null) {
                        olympiads.append(" (").append(olympiad.getDate().format(dateFormatter)).append(")");
                    }
                    olympiads.append("; ");
                }
                if (olympiads.length() > 2) olympiads.setLength(olympiads.length() - 2);
            }
            cell8.setCellValue(olympiads.toString());
            if (rowStyle != null) cell8.setCellStyle(rowStyle);
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_simple_export_" + LocalDate.now() + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new ByteArrayResource(baos.toByteArray()));
    }
}