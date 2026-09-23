package br.com.devgui.reservationapi.api.dto.error;

public record FieldErrorDetail(
        String field,
        String issue
) {
}
