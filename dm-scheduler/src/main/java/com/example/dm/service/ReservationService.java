package com.example.dm.service;

import com.example.dm.entity.Reservation;
import java.util.List;

public interface ReservationService {
    Reservation getReservationById(Integer id);
    List<Reservation> getReservationsByUserId(Integer userId);
    List<Reservation> getReservationsBySessionId(Integer sessionId);
    Reservation getReservationByUserAndSession(Integer userId, Integer sessionId);
    List<Reservation> getAllReservations();
    boolean addReservation(Reservation reservation);
    boolean updateReservation(Reservation reservation);
    boolean deleteReservation(Integer id);
    boolean cancelReservation(Integer id);
    int countConfirmedReservationsBySessionId(Integer sessionId);
}
