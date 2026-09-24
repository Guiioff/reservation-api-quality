package br.com.devgui.reservationapi.api.controller;

import br.com.devgui.reservationapi.api.dto.request.CreateReservationRequest;
import br.com.devgui.reservationapi.api.dto.response.PageDTO;
import br.com.devgui.reservationapi.api.dto.response.ReservationCompleteResponse;
import br.com.devgui.reservationapi.api.mapper.ReservationMapper;
import br.com.devgui.reservationapi.domain.exception.InvalidReservationException;
import br.com.devgui.reservationapi.domain.exception.InvalidReservationStatusException;
import br.com.devgui.reservationapi.domain.exception.ReservationConflictException;
import br.com.devgui.reservationapi.domain.exception.ResourceNotFoundException;
import br.com.devgui.reservationapi.domain.model.Reservation;
import br.com.devgui.reservationapi.domain.model.enums.ReservationStatus;
import br.com.devgui.reservationapi.domain.service.ReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
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

    @Test
    @DisplayName("Given existing id When find reservation by id Then return 200 ok")
    void givenExistingId_WhenFindReservationById_ThenReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation(
                id, "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 9, 25, 10, 0),
                LocalDateTime.of(2026, 9, 25, 12, 0), 3,
                ReservationStatus.PENDING, Instant.now(), Instant.now()
        );
        ReservationCompleteResponse responseDTO = new ReservationCompleteResponse(
                id, reservation.getCustomerName(), reservation.getCustomerEmail(),
                reservation.getStartAt(), reservation.getEndAt(), reservation.getPeople(),
                reservation.getStatus(), Instant.now(), Instant.now()
        );
        when(reservationService.findById(id)).thenReturn(reservation);
        when(reservationMapper.toCompleteResponse(reservation)).thenReturn(responseDTO);

        ResultActions response = mockMvc.perform(get(URL + "/{id}", id));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.customerName").value(reservation.getCustomerName()))
                .andExpect(jsonPath("$.customerEmail").value(reservation.getCustomerEmail()))
                .andExpect(jsonPath("$.startAt").isNotEmpty())
                .andExpect(jsonPath("$.endAt").isNotEmpty())
                .andExpect(jsonPath("$.people").value(reservation.getPeople()))
                .andExpect(jsonPath("$.status").value(reservation.getStatus().toString()));

        verify(reservationService).findById(id);
        verify(reservationMapper).toCompleteResponse(reservation);
    }

    @Test
    @DisplayName("Given no existing id When find reservation by id Then return 404 not found")
    void givenNoExistingId_WhenFindReservationById_ThenReturn404() throws Exception {
        UUID id = UUID.randomUUID();

        when(reservationService.findById(id)).thenThrow(
                new ResourceNotFoundException("Reservation not found with id " + id)
        );

        ResultActions response = mockMvc.perform(get(URL + "/{id}", id));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value("Resource not found"))
                .andExpect(jsonPath("$.message").value("Reservation not found with id " + id.toString()))
                .andExpect(jsonPath("$.path").value(URL + "/" + id.toString()));

        verify(reservationService).findById(id);
        verify(reservationMapper, never()).toCompleteResponse(any(Reservation.class));
    }

    @Test
    @DisplayName("Given existing reservations When find all reservations Then return 200 ok")
    void givenExistingReservations_WhenFindAllReservations_ThenReturn200() throws Exception {
        List<Reservation> reservations = List.of(
                new Reservation(UUID.randomUUID(), "Guilherme", "guilherme@email.com",
                        LocalDateTime.of(2026, 9, 25, 10, 0),
                        LocalDateTime.of(2026, 9, 25, 12, 0), 3,
                        ReservationStatus.PENDING, Instant.now(), Instant.now()),
                new Reservation(UUID.randomUUID(), "Gabriel Barros", "gabriel@email.com",
                        LocalDateTime.of(2026, 9, 25, 13, 0),
                        LocalDateTime.of(2026, 9, 25, 15, 0), 5,
                        ReservationStatus.PENDING, Instant.now(), Instant.now())
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> reservationPage = new PageImpl<>(reservations, pageable, reservations.size());

        List<ReservationCompleteResponse> reservationCompleteResponses = List.of(
                new ReservationCompleteResponse(
                        reservations.get(0).getId(), reservations.get(0).getCustomerName(),
                        reservations.get(0).getCustomerEmail(), reservations.get(0).getStartAt(),
                        reservations.get(0).getEndAt(), reservations.get(0).getPeople(),
                        reservations.get(0).getStatus(), reservations.get(0).getCreatedAt(),reservations.get(0).getUpdatedAt()
                ),
                new ReservationCompleteResponse(
                        reservations.get(1).getId(), reservations.get(1).getCustomerName(),
                        reservations.get(1).getCustomerEmail(), reservations.get(1).getStartAt(),
                        reservations.get(1).getEndAt(), reservations.get(1).getPeople(),
                        reservations.get(1).getStatus(), reservations.get(1).getCreatedAt(),reservations.get(1).getUpdatedAt()
                )
        );
        PageDTO<ReservationCompleteResponse> pageDTO = new PageDTO<>(
                reservationCompleteResponses,
                (int) reservationPage.getTotalElements(),
                reservationPage.getTotalPages(),
                reservationPage.getPageable().getPageNumber(),
                reservationPage.getPageable().getPageSize()
        );

        when(reservationService.findAll(pageable)).thenReturn(reservationPage);
        when(reservationMapper.toCompletePageDTO(reservationPage)).thenReturn(pageDTO);

        ResultActions response = mockMvc.perform(get(URL));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content.length()").value(reservations.size()))
                .andExpect(jsonPath("$.content[0].id").value(reservations.get(0).getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(reservations.get(1).getId().toString()))
                .andExpect(jsonPath("$.totalElements").value(reservationPage.getTotalElements()))
                .andExpect(jsonPath("$.totalPages").value(reservationPage.getTotalPages()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(pageable.getPageSize()));

        verify(reservationService).findAll(pageable);
        verify(reservationMapper).toCompletePageDTO(reservationPage);
    }

    @Test
    @DisplayName("Given no existing reservations When find all reservations Then return 200 ok")
    void givenNoExistingReservations_WhenFindAllReservations_ThenReturn200() throws Exception {
        List<Reservation> reservations = List.of();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> reservationPage = new PageImpl<>(reservations, pageable, reservations.size());

        PageDTO<ReservationCompleteResponse> pageDTO = new PageDTO<>(
                List.of(),
                (int) reservationPage.getTotalElements(),
                reservationPage.getTotalPages(),
                reservationPage.getPageable().getPageNumber(),
                reservationPage.getPageable().getPageSize()
        );

        when(reservationService.findAll(pageable)).thenReturn(reservationPage);
        when(reservationMapper.toCompletePageDTO(reservationPage)).thenReturn(pageDTO);

        ResultActions response = mockMvc.perform(get(URL));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(pageable.getPageSize()));

        verify(reservationService).findAll(pageable);
        verify(reservationMapper).toCompletePageDTO(reservationPage);
    }

    @Test
    @DisplayName("Given valid reservation id with pending status When confirm reservation Then return 200 ok")
    void givenValidReservationIdWithPendingStatus_WhenConfirmReservation_ThenReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation(
                id, "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 9, 25, 10, 0),
                LocalDateTime.of(2026, 9, 25, 12, 0), 3,
                ReservationStatus.CONFIRMED, Instant.now(), Instant.now()
        );
        ReservationCompleteResponse responseDTO = new ReservationCompleteResponse(
                id, reservation.getCustomerName(), reservation.getCustomerEmail(),
                reservation.getStartAt(), reservation.getEndAt(), reservation.getPeople(),
                reservation.getStatus(), Instant.now(), Instant.now()
        );

        when(reservationService.confirm(id)).thenReturn(reservation);
        when(reservationMapper.toCompleteResponse(reservation)).thenReturn(responseDTO);

        ResultActions response = mockMvc.perform(patch(URL + "/{id}/confirm", id));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.customerName").value(reservation.getCustomerName()))
                .andExpect(jsonPath("$.customerEmail").value(reservation.getCustomerEmail()))
                .andExpect(jsonPath("$.startAt").isNotEmpty())
                .andExpect(jsonPath("$.endAt").isNotEmpty())
                .andExpect(jsonPath("$.people").value(reservation.getPeople()))
                .andExpect(jsonPath("$.status").value(ReservationStatus.CONFIRMED.toString()));

        verify(reservationService).confirm(id);
        verify(reservationMapper).toCompleteResponse(reservation);
    }

    @Test
    @DisplayName("Given valid reservation id with invalid status When Confirm Reservation Then Return 422 unprocessable content")
    void givenValidReservationIdWithInvalidStatus_WhenConfirmReservation_ThenReturn422() throws Exception {
        UUID id = UUID.randomUUID();

        when(reservationService.confirm(id)).thenThrow(
                new InvalidReservationStatusException("Reservation with id " + id +
                        " cannot be confirmed because its status is invalid for this operation"));

        ResultActions response = mockMvc.perform(patch(URL + "/{id}/confirm", id));

        response
                .andDo(print())
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(jsonPath("$.error").value("Business rule violation"))
                .andExpect(jsonPath("$.message").value(
                        "Reservation with id " + id +
                                " cannot be confirmed because its status is invalid for this operation"))
                .andExpect(jsonPath("$.path").value(URL + "/" + id + "/confirm"));

        verify(reservationService).confirm(id);
        verify(reservationMapper, never()).toCompleteResponse(any(Reservation.class));
    }

    @Test
    @DisplayName("Given no existing id When Confirm Reservation Then Return 404 not found")
    void givenNoExistingId_WhenConfirmReservation_ThenReturn404() throws Exception {
        UUID id = UUID.randomUUID();

        when(reservationService.confirm(id)).thenThrow(
                new ResourceNotFoundException("Reservation not found with id " + id));

        ResultActions response = mockMvc.perform(patch(URL + "/{id}/confirm", id));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value("Resource not found"))
                .andExpect(jsonPath("$.message").value("Reservation not found with id " + id))
                .andExpect(jsonPath("$.path").value(URL + "/" + id + "/confirm"));

        verify(reservationService).confirm(id);
        verify(reservationMapper, never()).toCompleteResponse(any(Reservation.class));
    }

    @Test
    @DisplayName("Given valid reservation id with valid status When cancel reservation Then return 200 ok")
    void givenValidReservationIdWithValidStatus_WhenCancelReservation_ThenReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation(
                id, "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 9, 25, 10, 0),
                LocalDateTime.of(2026, 9, 25, 12, 0), 3,
                ReservationStatus.CANCELLED, Instant.now(), Instant.now()
        );
        ReservationCompleteResponse responseDTO = new ReservationCompleteResponse(
                id, reservation.getCustomerName(), reservation.getCustomerEmail(),
                reservation.getStartAt(), reservation.getEndAt(), reservation.getPeople(),
                reservation.getStatus(), Instant.now(), Instant.now()
        );

        when(reservationService.cancel(id)).thenReturn(reservation);
        when(reservationMapper.toCompleteResponse(reservation)).thenReturn(responseDTO);

        ResultActions response = mockMvc.perform(patch(URL + "/{id}/cancel", id));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.customerName").value(reservation.getCustomerName()))
                .andExpect(jsonPath("$.customerEmail").value(reservation.getCustomerEmail()))
                .andExpect(jsonPath("$.startAt").isNotEmpty())
                .andExpect(jsonPath("$.endAt").isNotEmpty())
                .andExpect(jsonPath("$.people").value(reservation.getPeople()))
                .andExpect(jsonPath("$.status").value(ReservationStatus.CANCELLED.toString()));

        verify(reservationService).cancel(id);
        verify(reservationMapper).toCompleteResponse(reservation);
    }

    @Test
    @DisplayName("Given valid reservation id with invalid status When cancel reservation Then return 422 unprocessable content")
    void givenValidReservationIdWithInvalidStatus_WhenCancelReservation_ThenReturn422() throws Exception {
        UUID id = UUID.randomUUID();

        when(reservationService.cancel(id)).thenThrow(
                new InvalidReservationStatusException("Reservation with id " + id +
                                " cannot be cancelled its status is invalid for this operation"));

        ResultActions response = mockMvc.perform(patch(URL + "/{id}/cancel", id));

        response
                .andDo(print())
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(jsonPath("$.error").value("Business rule violation"))
                .andExpect(jsonPath("$.message").value(
                        "Reservation with id " + id +
                                " cannot be cancelled its status is invalid for this operation"))
                .andExpect(jsonPath("$.path").value(URL + "/" + id + "/cancel"));

        verify(reservationService).cancel(id);
        verify(reservationMapper, never()).toCompleteResponse(any(Reservation.class));
    }

    @Test
    @DisplayName("Given no existing id When cancel reservation Then return 404 not found")
    void givenNoExistingId_WhenCancelReservation_ThenReturn404() throws Exception {
        UUID id = UUID.randomUUID();

        when(reservationService.cancel(id)).thenThrow(
                new ResourceNotFoundException("Reservation not found with id " + id));

        ResultActions response = mockMvc.perform(patch(URL + "/{id}/cancel", id));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value("Resource not found"))
                .andExpect(jsonPath("$.message").value("Reservation not found with id " + id))
                .andExpect(jsonPath("$.path").value(URL + "/" + id + "/cancel"));

        verify(reservationService).cancel(id);
        verify(reservationMapper, never()).toCompleteResponse(any(Reservation.class));
    }
}