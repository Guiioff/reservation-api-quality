package br.com.devgui.reservationapi.api.dto.response;

import br.com.devgui.reservationapi.domain.model.enums.ReservationStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationCompleteResponse(
        UUID id,
        String customerName,
        String customerEmail,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer people,
        ReservationStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
