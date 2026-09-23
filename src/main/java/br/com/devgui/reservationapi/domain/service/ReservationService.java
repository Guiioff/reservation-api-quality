package br.com.devgui.reservationapi.domain.service;

import br.com.devgui.reservationapi.domain.model.Reservation;
import br.com.devgui.reservationapi.infraestructure.repository.ReservationRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }
}
