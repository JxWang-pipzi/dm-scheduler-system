package com.example.dm.mapper;

import com.example.dm.entity.Reservation;
import java.util.List;

public interface ReservationMapper {
    Reservation selectById(Integer id);
    List<Reservation> selectByUserId(Integer userId);
    List<Reservation> selectBySessionId(Integer sessionId);
    Reservation selectByUserAndSession(Integer userId, Integer sessionId);
    List<Reservation> selectAll();
    int insert(Reservation reservation);
    int update(Reservation reservation);
    int delete(Integer id);
    int countBySessionIdAndStatus(Integer sessionId, String status);
}
