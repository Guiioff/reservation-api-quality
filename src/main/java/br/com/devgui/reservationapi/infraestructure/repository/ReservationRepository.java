package br.com.devgui.reservationapi.infraestructure.repository;

import br.com.devgui.reservationapi.domain.model.Reservation;
import br.com.devgui.reservationapi.domain.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @Query("""
        SELECT COUNT(r) > 0 FROM Reservation r
        WHERE r.startAt < :endAt
          AND r.endAt > :startAt
          AND r.status IN :statuses
    """)
    boolean existsConflict(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("statuses") Collection<ReservationStatus> statuses
    );
}
