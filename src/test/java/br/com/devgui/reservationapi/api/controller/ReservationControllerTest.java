package br.com.devgui.reservationapi.api.controller;

import br.com.devgui.reservationapi.api.dto.request.CreateReservationRequest;
import br.com.devgui.reservationapi.api.dto.response.ReservationCompleteResponse;
import br.com.devgui.reservationapi.api.mapper.ReservationMapper;
import br.com.devgui.reservationapi.domain.exception.InvalidReservationException;
import br.com.devgui.reservationapi.domain.exception.ReservationConflictException;
import br.com.devgui.reservationapi.domain.model.Reservation;
import br.com.devgui.reservationapi.domain.model.enums.ReservationStatus;
import br.com.devgui.reservationapi.domain.service.ReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ReservationService reservationService;

    @MockitoBean
    ReservationMapper reservationMapper;

    private final String URL = "/api/reservations";

    @Test
    @DisplayName("Given valid request When create reservation Then return 201 Created")
    void givenValidRequest_WhenCreateReservation_ThenReturn201() throws Exception {
        UUID id = UUID.randomUUID();

        CreateReservationRequest requestDTO = new CreateReservationRequest(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 9, 25, 10, 0),
                LocalDateTime.of(2026, 9, 25, 12, 0),
                3
        );
        Reservation reservation = new Reservation(
                id, "Guilherme", "guilherme@email.com",
                requestDTO.startAt(), requestDTO.endAt(), 3,
                ReservationStatus.PENDING, Instant.now(), Instant.now()
        );
        ReservationCompleteResponse responseDTO = new ReservationCompleteResponse(
                id, "Guilherme", "guilherme@email.com",
                requestDTO.startAt(), requestDTO.endAt(), 3,
                ReservationStatus.PENDING, Instant.now(), Instant.now()
        );

        when(reservationMapper.toEntity(any(CreateReservationRequest.class))).thenReturn(reservation);
        when(reservationService.create(reservation)).thenReturn(reservation);
        when(reservationMapper.toCompleteResponse(reservation)).thenReturn(responseDTO);

        ResultActions response = mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)));
        response
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost" + URL + "/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.customerName").value(requestDTO.customerName()))
                .andExpect(jsonPath("$.customerEmail").value(requestDTO.customerEmail()))
                .andExpect(jsonPath("$.startAt").isNotEmpty())
                .andExpect(jsonPath("$.endAt").isNotEmpty())
                .andExpect(jsonPath("$.people").value(requestDTO.people()))
                .andExpect(jsonPath("$.status").value(ReservationStatus.PENDING.toString()));

        verify(reservationMapper).toEntity(requestDTO);
        verify(reservationService).create(reservation);
        verify(reservationMapper).toCompleteResponse(reservation);
    }

    @Test
    @DisplayName("Given invalid request When create reservation Then return 400 bad request")
    void givenInvalidRequest_WhenCreateReservation_ThenReturn400() throws Exception {
        CreateReservationRequest requestDTO = new CreateReservationRequest(
                "", "",
                LocalDateTime.of(2026, 9, 25, 10, 0),
                LocalDateTime.of(2026, 9, 25, 12, 0),
                0
        );

        ResultActions response = mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)));
        response
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.path").value(URL))
                .andExpect(jsonPath("$.details").isNotEmpty())
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details.length()").value(3));

        verify(reservationMapper, never()).toEntity(any(CreateReservationRequest.class));
        verify(reservationService, never()).create(any(Reservation.class));
        verify(reservationMapper, never()).toCompleteResponse(any(Reservation.class));
    }

    @Test
    @DisplayName("Given valid request with invalid date range When create reservation Then return 422 unprocessable content")
    void givenValidRequestWithInvalidDateRange_WhenCreateReservation_ThenReturn422() throws Exception {
        CreateReservationRequest requestDTO = new CreateReservationRequest(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 9, 28, 10, 0),
                LocalDateTime.of(2026, 9, 25, 12, 0),
                3
        );
        Reservation reservation = new Reservation(
                requestDTO.customerName(), requestDTO.customerEmail(),
                requestDTO.startAt(), requestDTO.endAt(), requestDTO.people()
        );

        when(reservationMapper.toEntity(any(CreateReservationRequest.class))).thenReturn(reservation);
        when(reservationService.create(reservation))
                .thenThrow(new InvalidReservationException("Invalid start time range"));

        ResultActions response = mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)));

        response
                .andDo(print())
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.statusCode").value(422))
                .andExpect(jsonPath("$.error").value("Business rule violation"))
                .andExpect(jsonPath("$.message").value("Invalid start time range"))
                .andExpect(jsonPath("$.path").value(URL));

        verify(reservationMapper).toEntity(eq(requestDTO));
        verify(reservationService).create(reservation);
        verify(reservationMapper, never()).toCompleteResponse(any(Reservation.class));
    }

    @Test
    @DisplayName("Given valid request with conflicting time range When create reservation Then return 409 conflict")
    void givenValidRequestWithConflictingTimeRange_WhenCreateReservation_ThenReturn409() throws Exception {
        CreateReservationRequest requestDTO = new CreateReservationRequest(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 9, 25, 10, 0),
                LocalDateTime.of(2026, 9, 25, 12, 0),
                3
        );
        Reservation reservation = new Reservation(
                requestDTO.customerName(), requestDTO.customerEmail(),
                requestDTO.startAt(), requestDTO.endAt(), requestDTO.people()
        );

        when(reservationMapper.toEntity(any(CreateReservationRequest.class))).thenReturn(reservation);
        when(reservationService.create(reservation))
                .thenThrow(new ReservationConflictException("There is already a reservation in this time range"));

        ResultActions response = mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)));

        response
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.error").value("Reservation conflict"))
                .andExpect(jsonPath("$.message").value("There is already a reservation in this time range"))
                .andExpect(jsonPath("$.path").value(URL));

        verify(reservationMapper).toEntity(eq(requestDTO));
        verify(reservationService).create(reservation);
        verify(reservationMapper, never()).toCompleteResponse(any(Reservation.class));
    }
}