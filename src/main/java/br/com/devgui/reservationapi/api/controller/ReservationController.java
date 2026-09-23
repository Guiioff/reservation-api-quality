package br.com.devgui.reservationapi.api.controller;

import br.com.devgui.reservationapi.api.dto.request.CreateReservationRequest;
import br.com.devgui.reservationapi.api.dto.response.ReservationCompleteResponse;
import br.com.devgui.reservationapi.api.mapper.ReservationMapper;
import br.com.devgui.reservationapi.domain.model.Reservation;
import br.com.devgui.reservationapi.domain.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController()
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    public ReservationController(ReservationService reservationService, ReservationMapper reservationMapper) {
        this.reservationService = reservationService;
        this.reservationMapper = reservationMapper;
    }

    @PostMapping
    public ResponseEntity<ReservationCompleteResponse> create(@RequestBody @Valid CreateReservationRequest dto) {
        Reservation reservation = reservationMapper.toEntity(dto);
        Reservation savedReservation = reservationService.create(reservation);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedReservation.getId())
                .toUri();

        ReservationCompleteResponse response = reservationMapper.toCompleteResponse(savedReservation);
        return ResponseEntity.created(location).body(response);
    }
}
