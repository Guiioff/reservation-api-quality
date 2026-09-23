package br.com.devgui.reservationapi.api.exception;

import br.com.devgui.reservationapi.api.dto.error.FieldErrorDetail;
import br.com.devgui.reservationapi.api.dto.error.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDetail> errorDetails = ex.getFieldErrors()
                .stream()
                .map(err -> new FieldErrorDetail(
                        err.getField(),
                        err.getDefaultMessage()
                ))
                .toList();

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                "Invalid request",
                request.getRequestURI(),
                errorDetails
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }
}
