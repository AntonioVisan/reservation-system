package org.acme.protocol;

public class CancelCommand implements Command{
    private Long reservationId;

    public CancelCommand(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getReservationId(){
        return reservationId;
    }
}
