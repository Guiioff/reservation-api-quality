package br.com.devgui.reservationapi.infraestructure.repository;

import br.com.devgui.reservationapi.domain.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
}
