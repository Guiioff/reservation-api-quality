package br.com.devgui.reservationapi.api.dto.error;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        Integer statusCode,
        String error,
        String message,
        String path
) {
}
