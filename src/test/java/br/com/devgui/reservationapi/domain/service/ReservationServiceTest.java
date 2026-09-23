package br.com.devgui.reservationapi.domain.service;

import br.com.devgui.reservationapi.domain.exception.InvalidReservationException;
import br.com.devgui.reservationapi.domain.exception.ReservationConflictException;
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

import java.time.LocalDateTime;
import java.util.List;

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
}