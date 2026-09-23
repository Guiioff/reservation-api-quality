package br.com.devgui.reservationapi.domain.service;

import br.com.devgui.reservationapi.domain.exception.InvalidReservationException;
import br.com.devgui.reservationapi.domain.exception.ReservationConflictException;
import br.com.devgui.reservationapi.domain.exception.ResourceNotFoundException;
import br.com.devgui.reservationapi.domain.model.Reservation;
import br.com.devgui.reservationapi.domain.model.enums.ReservationStatus;
import br.com.devgui.reservationapi.infraestructure.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation create(Reservation reservation) {
        if (!reservation.getStartAt().isBefore(reservation.getEndAt())) {
            throw new InvalidReservationException("Invalid start time range");
        }

        if (reservation.getPeople() <= 0){
            throw new InvalidReservationException("Invalid number of people");
        }

        boolean hasConflict = reservationRepository.existsConflict(
                reservation.getStartAt(),
                reservation.getEndAt(),
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));

        if (hasConflict){
            throw new ReservationConflictException("There is already a reservation in this time range");
        }

        return reservationRepository.save(reservation);
    }

    public Reservation findById(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id " + id));
        return reservation;
    }
}
