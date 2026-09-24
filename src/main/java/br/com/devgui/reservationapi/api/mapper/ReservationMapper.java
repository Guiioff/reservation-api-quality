package br.com.devgui.reservationapi.api.mapper;

import br.com.devgui.reservationapi.api.dto.request.CreateReservationRequest;
import br.com.devgui.reservationapi.api.dto.response.PageDTO;
import br.com.devgui.reservationapi.api.dto.response.ReservationCompleteResponse;
import br.com.devgui.reservationapi.domain.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationMapper {

    public Reservation toEntity(CreateReservationRequest dto){
        return new Reservation(
                dto.customerName(), dto.customerEmail(), dto.startAt(), dto.endAt(), dto.people()
        );
    }

    public ReservationCompleteResponse toCompleteResponse(Reservation reservation){
        return new ReservationCompleteResponse(
                reservation.getId(), reservation.getCustomerName(), reservation.getCustomerEmail(),
                reservation.getStartAt(), reservation.getEndAt(), reservation.getPeople(),
                reservation.getStatus(), reservation.getCreatedAt(), reservation.getUpdatedAt()
        );
    }

    public PageDTO<ReservationCompleteResponse> toCompletePageDTO(Page<Reservation> page){
        List<ReservationCompleteResponse> content = page.getContent()
                .stream()
                .map(r -> this.toCompleteResponse(r))
                .toList();
        return new PageDTO<ReservationCompleteResponse>(
                content,
                page.getNumberOfElements(),
                page.getTotalPages(),
                page.getPageable().getPageNumber(),
                page.getPageable().getPageSize()
        );
    }
}
