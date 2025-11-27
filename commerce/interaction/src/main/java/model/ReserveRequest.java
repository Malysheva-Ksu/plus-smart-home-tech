package model;

import java.util.Map;

public class ReserveRequest {

    private Map<Long, Integer> reservations;

    public ReserveRequest() {
    }

    public ReserveRequest(Map<Long, Integer> reservations) {
        this.reservations = reservations;
    }

    public Map<Long, Integer> getReservations() {
        return reservations;
    }

    public void setReservations(Map<Long, Integer> reservations) {
        this.reservations = reservations;
    }
}