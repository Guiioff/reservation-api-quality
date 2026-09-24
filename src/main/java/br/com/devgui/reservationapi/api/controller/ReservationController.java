package br.com.devgui.reservationapi.api.controller;

import br.com.devgui.reservationapi.api.dto.request.CreateReservationRequest;
import br.com.devgui.reservationapi.api.dto.response.PageDTO;
import br.com.devgui.reservationapi.api.dto.response.ReservationCompleteResponse;
import br.com.devgui.reservationapi.api.mapper.ReservationMapper;
import br.com.devgui.reservationapi.domain.model.Reservation;
import br.com.devgui.reservationapi.domain.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

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

    @GetMapping("/{id}")
    public ResponseEntity<ReservationCompleteResponse> getById(@PathVariable UUID id) {
        Reservation reservation = reservationService.findById(id);
        ReservationCompleteResponse response = reservationMapper.toCompleteResponse(reservation);
        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<PageDTO<ReservationCompleteResponse>> getAll(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<Reservation> reservationPage = reservationService.findAll(pageable);
        PageDTO<ReservationCompleteResponse> response = reservationMapper.toCompletePageDTO(reservationPage);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ReservationCompleteResponse> confirm(@PathVariable UUID id) {
        Reservation reservation = reservationService.confirm(id);
        ReservationCompleteResponse response = reservationMapper.toCompleteResponse(reservation);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationCompleteResponse> cancel(@PathVariable UUID id) {
        Reservation reservation = reservationService.cancel(id);
        ReservationCompleteResponse response = reservationMapper.toCompleteResponse(reservation);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ReservationCompleteResponse> complete(@PathVariable UUID id) {
        Reservation reservation = reservationService.complete(id);
        ReservationCompleteResponse response = reservationMapper.toCompleteResponse(reservation);
        return ResponseEntity.ok(response);
    }
}
