package br.com.devgui.reservationapi.domain.service;

import br.com.devgui.reservationapi.domain.exception.InvalidReservationException;
import br.com.devgui.reservationapi.domain.exception.InvalidReservationStatusException;
import br.com.devgui.reservationapi.domain.exception.ReservationConflictException;
import br.com.devgui.reservationapi.domain.exception.ResourceNotFoundException;
import br.com.devgui.reservationapi.domain.model.Reservation;
import br.com.devgui.reservationapi.domain.model.enums.ReservationStatus;
import br.com.devgui.reservationapi.infraestructure.repository.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("Given Valid Reservation When Create Reservation Then Return Saved Reservation")
    void givenValidReservation_WhenCreateReservation_ThenReturnSavedReservation() {
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 18, 10, 0),
                LocalDateTime.of(2026, 7, 20, 10, 0),
                3
        );
        when(reservationRepository.save(reservation)).thenReturn(reservation);
        when(reservationRepository.existsConflict(
                reservation.getStartAt(),
                reservation.getEndAt(),
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        )).thenReturn(false);

        Reservation savedReservation = reservationService.create(reservation);

        assertNotNull(savedReservation);
        verify(reservationRepository).save(reservation);
    }

    @Test
    @DisplayName("Given Invalid Date Range When Create Reservation Then Throw InvalidReservationException")
    void givenInvalidDateRange_WhenCreateReservation_ThenThrowInvalidReservationException() {
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 18, 10, 0),
                3
        );

        assertThrows(InvalidReservationException.class, () ->
            reservationService.create(reservation)
        );
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Given Equal Start And End When Create Reservation Then Throw InvalidReservationException")
    void givenEqualStartAndEnd_WhenCreateReservation_ThenThrowInvalidReservationException() {
        Reservation reservation = new Reservation(
                "Guilherme",
                "guilherme@email.com",
                LocalDateTime.of(2026, 7, 18, 10, 0),
                LocalDateTime.of(2026, 7, 18, 10, 0),
                3
        );

        assertThrows(InvalidReservationException.class, () ->
                reservationService.create(reservation)
        );
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @ParameterizedTest
    @CsvSource({"0", "-1"})
    @DisplayName("Given Invalid People Quantity When Create Reservation Then Throw InvalidReservationException")
    void givenInvalidPeopleQuantity_WhenCreateReservation_ThenThrowInvalidReservationException(Integer peopleQuantity) {
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 18, 10, 0),
                LocalDateTime.of(2026, 7, 20, 10, 0),
                peopleQuantity
        );

        assertThrows(InvalidReservationException.class, () ->
            reservationService.create(reservation)
        );
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Given Conflicting Reservation When Create Reservation Then Throw ReservationConflictException")
    void givenConflictingReservation_WhenCreateReservation_ThenThrowReservationConflictException() {
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 18, 10, 0),
                LocalDateTime.of(2026, 7, 20, 10, 0),
                3
        );

        when(reservationRepository.existsConflict(
                reservation.getStartAt(), reservation.getEndAt(),
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        )).thenReturn(true);

        assertThrows(ReservationConflictException.class, () ->
            reservationService.create(reservation)
        );
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Given Valid Id When FindById Then Return Reservation")
    void givenValidId_WhenFindById_ThenReturnReservation() {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 18, 10, 0),
                LocalDateTime.of(2026, 7, 20, 10, 0),
                3
        );

        when(reservationRepository.findById(id))
                .thenReturn(Optional.of(reservation));

        Reservation returnedReservation = reservationService.findById(id);

        assertNotNull(returnedReservation);
        assertEquals(reservation.getCustomerName(), returnedReservation.getCustomerName());
        assertEquals(reservation.getCustomerEmail(), returnedReservation.getCustomerEmail());
        verify(reservationRepository).findById(id);
    }

    @Test
    @DisplayName("Given No Existing Id When FindById Then Throw ResourceNotFoundException")
    void givenNoExistingId_WhenFindById_ThenThrowResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(reservationRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.findById(id));
    }

    @Test
    @DisplayName("Given Reservations List When FindAll Then Return Reservations Page")
    void givenReservationsList_WhenFindAll_ThenReturnReservationsPage() {
        Reservation reservation1 = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 18, 10, 0),
                LocalDateTime.of(2026, 7, 20, 10, 0),
                3
        );
        Reservation reservation2 = new Reservation(
                "Leandro", "leandro@email.com",
                LocalDateTime.of(2026, 12, 23, 10, 0),
                LocalDateTime.of(2027, 1, 2, 10, 0),
                5
        );
        List<Reservation> reservations = List.of(reservation1, reservation2);
        Pageable pageable = PageRequest.of(0, 2);

        when(reservationRepository.findAll(pageable)).thenReturn(new PageImpl<>(reservations));

        Page<Reservation> returnedReservations = reservationService.findAll(pageable);

        assertNotNull(returnedReservations);
        assertFalse(returnedReservations.isEmpty());
        assertEquals(reservations, returnedReservations.getContent());
        assertEquals(reservations.size(), returnedReservations.getTotalElements());
    }

    @Test
    @DisplayName("Given Empty Reservations List When FindAll Then Return Empty Reservations Page")
    void givenEmptyReservationsList_WhenFindAll_ThenReturnEmptyReservationsPage() {
        Pageable pageable = PageRequest.of(0, 2);
        when(reservationRepository.findAll(pageable)).thenReturn(Page.empty());

        Page<Reservation> returnedReservations = reservationService.findAll(pageable);

        assertNotNull(returnedReservations);
        assertTrue(returnedReservations.isEmpty());
    }

    @Test
    @DisplayName("Given Pending Reservation When Confirm Then Return A Valid Reservation")
    void givenPendingReservation_WhenConfirm_ThenReturnAValidReservation() {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 18, 10, 0),
                3, ReservationStatus.PENDING
        );

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        Reservation returnedReservation = reservationService.confirm(id);

        assertNotNull(returnedReservation);
        assertEquals(ReservationStatus.CONFIRMED, returnedReservation.getStatus());
        assertEquals(reservation.getCustomerName(), returnedReservation.getCustomerName());
        assertEquals(reservation.getCustomerEmail(), returnedReservation.getCustomerEmail());
    }

    @Test
    @DisplayName("Given Cancelled Reservation When Confirm Then Throw InvalidReservationStatusException")
    void givenCancelledReservation_WhenConfirm_ThenThrowInvalidReservationStatusException() {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 18, 10, 0),
                3, ReservationStatus.CANCELLED
        );

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));

        assertThrows(InvalidReservationStatusException.class,
                () -> reservationService.confirm(id));

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Given Completed Reservation When Confirm Then Throw InvalidReservationStatusException")
    void givenCompletedReservation_WhenConfirm_ThenThrowInvalidReservationStatusException() {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 18, 10, 0),
                3, ReservationStatus.COMPLETED
        );

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));

        assertThrows(InvalidReservationStatusException.class,
                () -> reservationService.confirm(id));

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Given Confirmed Reservation When Confirm Then Throw InvalidReservationStatusException")
    void givenConfirmedReservation_WhenConfirm_ThenThrowInvalidReservationStatusException() {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 18, 10, 0),
                3, ReservationStatus.CONFIRMED
        );

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));

        assertThrows(InvalidReservationStatusException.class,
                () -> reservationService.confirm(id));

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Given No Existing Id When Confirm Then Throw ResourceNotFoundException")
    void givenNoExistingId_WhenConfirm_ThenThrowResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 18, 10, 0),
                3, ReservationStatus.PENDING
        );

        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.confirm(id));

        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}