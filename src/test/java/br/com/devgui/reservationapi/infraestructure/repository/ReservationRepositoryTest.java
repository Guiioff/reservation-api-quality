package br.com.devgui.reservationapi.infraestructure.repository;

import br.com.devgui.reservationapi.domain.model.Reservation;
import br.com.devgui.reservationapi.domain.model.enums.ReservationStatus;
import br.com.devgui.reservationapi.testconfig.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("Given Overlapping Reservation When Check Conflict Then Return True")
    void givenOverlappingReservation_WhenCheckConflict_ThenReturnTrue(){
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 20, 12, 0),
                3, ReservationStatus.PENDING
        );
        reservationRepository.save(reservation);

        boolean conflict = reservationRepository.existsConflict(
                LocalDateTime.of(2026, 7, 20, 11, 0),
                LocalDateTime.of(2026, 7, 20, 13, 0),
                List.of(
                        ReservationStatus.PENDING,
                        ReservationStatus.CONFIRMED
                )
        );

        assertTrue(conflict);
    }

    @Test
    @DisplayName("Given Non Overlapping Reservation When Check Conflict Then Return False")
    void givenNonOverlappingReservation_WhenCheckConflict_ThenReturnFalse() {
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 20, 12, 0),
                3, ReservationStatus.PENDING
        );
        reservationRepository.save(reservation);

        boolean conflict = reservationRepository.existsConflict(
                LocalDateTime.of(2026, 7, 20, 12, 0),
                LocalDateTime.of(2026, 7, 20, 14, 0),
                List.of(
                        ReservationStatus.PENDING,
                        ReservationStatus.CONFIRMED
                )
        );

        assertFalse(conflict);
    }

    @Test
    @DisplayName("Given Reservation With Ignored Status When Check Conflict Then Return False")
    void givenReservationWithIgnoredStatus_WhenCheckConflict_ThenReturnFalse() {
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 20, 12, 0),
                3, ReservationStatus.CANCELLED
        );
        reservationRepository.save(reservation);

        boolean conflict = reservationRepository.existsConflict(
                LocalDateTime.of(2026, 7, 20, 11, 0),
                LocalDateTime.of(2026, 7, 20, 13, 0),
                List.of(
                        ReservationStatus.PENDING,
                        ReservationStatus.CONFIRMED
                )
        );

        assertFalse(conflict);
    }

}