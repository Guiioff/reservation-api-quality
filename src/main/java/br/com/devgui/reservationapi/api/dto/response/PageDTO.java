package br.com.devgui.reservationapi.api.dto.response;

import java.util.List;

public record PageDTO<T>(
        List<T> content,
        int totalElements,
        int totalPages,
        int page,
        int size
) {
}
