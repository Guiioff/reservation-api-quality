package br.com.devgui.reservationapi.api.dto.error;

import java.time.Instant;
import java.util.List;

public record ValidationErrorResponse(
        Instant timestamp,
        Integer statusCode,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> details
) {
}
