package org.example.exception;

import org.example.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Обработка валидации (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ErrorResponse response = new ErrorResponse();
        response.setCode("VALIDATION_ERROR");
        response.setMessage("Validation failed: " + errors.toString());  // Или детализируй
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Обработка кастомных ошибок (400/404)
    @ExceptionHandler({EmailExistsException.class, UserNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleUserExceptions(RuntimeException ex) {
        ErrorResponse response = new ErrorResponse();
        response.setCode(ex.getClass().getSimpleName());  // EMAIL_EXISTS_EXCEPTION или USER_NOT_FOUND_EXCEPTION
        response.setMessage(ex.getMessage());
        HttpStatus status = ex instanceof UserNotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }

    // Обработка общих RuntimeException (500)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        // Логгируем в консоль
        ex.printStackTrace();
        ErrorResponse response = new ErrorResponse();
        response.setCode("INTERNAL_ERROR");
        response.setMessage("An unexpected error occurred: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Обработка других исключений (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ex.printStackTrace();
        ErrorResponse response = new ErrorResponse();
        response.setCode("GENERIC_ERROR");
        response.setMessage("Server error: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse response = new ErrorResponse();
        response.setCode("UNAUTHORIZED");
        response.setMessage("User not authenticated: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}