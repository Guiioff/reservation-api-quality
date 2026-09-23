package br.com.devgui.reservationapi.api.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateReservationRequest(
        @NotBlank(message = "Customer name is required")
        @Size(max = 255, message = "Customer name must not exceed 255 characters")
        String customerName,

        @NotBlank(message = "Customer email is required")
        @Size(max = 50, message = "Customer email must not exceed 50 characters")
        @Email(message = "Customer email must be a valid email address")
        String customerEmail,

        @NotNull(message = "Start date and time is required")
        LocalDateTime startAt,

        @NotNull(message = "End date and time is required")
        LocalDateTime endAt,

        @Min(value = 1, message = "Number of people must be at least 1")
        Integer people
) {
}
