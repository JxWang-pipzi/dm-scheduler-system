package com.example.dm.service.impl;

import com.example.dm.entity.Reservation;
import com.example.dm.entity.Order;
import com.example.dm.entity.Script;
import com.example.dm.entity.Session;
import com.example.dm.mapper.ReservationMapper;
import com.example.dm.mapper.SessionMapper;
import com.example.dm.service.OrderService;
import com.example.dm.service.ReservationService;
import com.example.dm.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ScriptService scriptService;

    @Override
    public Reservation getReservationById(Integer id) {
        return reservationMapper.selectById(id);
    }

    @Override
    public List<Reservation> getReservationsByUserId(Integer userId) {
        return reservationMapper.selectByUserId(userId);
    }

    @Override
    public List<Reservation> getReservationsBySessionId(Integer sessionId) {
        return reservationMapper.selectBySessionId(sessionId);
    }

    @Override
    public Reservation getReservationByUserAndSession(Integer userId, Integer sessionId) {
        return reservationMapper.selectByUserAndSession(userId, sessionId);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationMapper.selectAll();
    }

    @Override
    @Transactional
    public boolean addReservation(Reservation reservation) {
        if (reservation.getPlayersCount() == null || reservation.getPlayersCount() < 1) {
            reservation.setPlayersCount(1);
        }
        Session session = sessionMapper.selectById(reservation.getSessionId());
        if (session == null) {
            throw new IllegalStateException("场次不存在");
        }
        String sessionStatus = String.valueOf(session.getStatus()).trim().toUpperCase();
        if (!"PENDING".equals(sessionStatus)) {
            throw new IllegalStateException("该场次当前不可预约，请选择等待中场次");
        }

        int confirmedPlayers = countConfirmedReservationsBySessionId(reservation.getSessionId());
        int maxPlayers = session.getMaxPlayers() == null ? 0 : session.getMaxPlayers();
        int requestPlayers = reservation.getPlayersCount();
        if (maxPlayers < 1) {
            throw new IllegalStateException("场次容量配置异常");
        }
        if (confirmedPlayers + requestPlayers > maxPlayers) {
            int remaining = Math.max(maxPlayers - confirmedPlayers, 0);
            throw new IllegalStateException("场次余位不足，当前最多可预约 " + remaining + " 人");
        }

        Reservation existingReservation = reservationMapper.selectByUserAndSession(reservation.getUserId(), reservation.getSessionId());
        if (existingReservation != null) {
            String existingStatus = String.valueOf(existingReservation.getStatus()).trim().toUpperCase();
            if ("CONFIRMED".equals(existingStatus)) {
                throw new IllegalStateException("你已预约该场次，请勿重复预约");
            }
            if (!"CANCELLED".equals(existingStatus)) {
                throw new IllegalStateException("当前预约状态异常，请联系管理员");
            }
            existingReservation.setStatus("CONFIRMED");
            existingReservation.setPlayersCount(requestPlayers);
            int updated = reservationMapper.update(existingReservation);
            if (updated <= 0) {
                return false;
            }
            createPendingOrderForReservation(existingReservation, session);
            session.setCurrentPlayers(confirmedPlayers + requestPlayers);
            sessionMapper.update(session);
            return true;
        }

        int result = reservationMapper.insert(reservation);
        if (result > 0) {
            createPendingOrderForReservation(reservation, session);
            session.setCurrentPlayers(confirmedPlayers + requestPlayers);
            sessionMapper.update(session);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateReservation(Reservation reservation) {
        return reservationMapper.update(reservation) > 0;
    }

    @Override
    @Transactional
    public boolean deleteReservation(Integer id) {
        Reservation existing = reservationMapper.selectById(id);
        if (existing == null) {
            return false;
        }
        int result = reservationMapper.delete(id);
        if (result > 0) {
            cancelPendingOrderByUserAndSession(existing.getUserId(), existing.getSessionId());
            Session session = sessionMapper.selectById(existing.getSessionId());
            if (session != null) {
                int confirmedCount = countConfirmedReservationsBySessionId(existing.getSessionId());
                session.setCurrentPlayers(confirmedCount);
                sessionMapper.update(session);
            }
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean cancelReservation(Integer id) {
        // 获取预约信息
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            return false;
        }

        // 更新预约状态为取消
        reservation.setStatus("CANCELLED");
        int result = reservationMapper.update(reservation);
        if (result > 0) {
            cancelPendingOrderByUserAndSession(reservation.getUserId(), reservation.getSessionId());
            // 更新场次当前人数
            Session session = sessionMapper.selectById(reservation.getSessionId());
            if (session != null) {
                int confirmedCount = countConfirmedReservationsBySessionId(reservation.getSessionId());
                session.setCurrentPlayers(confirmedCount);
                sessionMapper.update(session);
            }
            return true;
        }
        return false;
    }

    @Override
    public int countConfirmedReservationsBySessionId(Integer sessionId) {
        return reservationMapper.countBySessionIdAndStatus(sessionId, "CONFIRMED");
    }

    private void createPendingOrderForReservation(Reservation reservation, Session session) {
        if (reservation == null || session == null) {
            throw new IllegalStateException("预约参数异常，无法生成订单");
        }
        Integer userId = reservation.getUserId();
        Integer sessionId = reservation.getSessionId();
        if (userId == null || sessionId == null) {
            throw new IllegalStateException("用户或场次信息缺失，无法生成订单");
        }

        // 避免重复生成同一用户同一场次订单
        List<Order> existingOrders = orderService.getByUserId(userId);
        if (existingOrders != null) {
            for (Order item : existingOrders) {
                if (item == null || !sessionId.equals(item.getSessionId())) {
                    continue;
                }
                String status = item.getStatus() == null ? "" : item.getStatus().trim().toUpperCase();
                if (!"CANCELLED".equals(status) && !"REFUNDED".equals(status)) {
                    return;
                }
            }
        }

        Script script = scriptService.getScriptById(session.getScriptId());
        if (script == null) {
            throw new IllegalStateException("关联剧本不存在，无法生成订单");
        }
        BigDecimal price = script.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("关联剧本价格无效，无法生成订单");
        }
        int playersCount = reservation.getPlayersCount() == null ? 1 : reservation.getPlayersCount();
        if (playersCount < 1) {
            playersCount = 1;
        }
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(playersCount));

        Order order = new Order();
        order.setOrderNo(generateOrderNo(userId, sessionId));
        order.setUserId(userId);
        order.setSessionId(sessionId);
        order.setTotalPrice(totalPrice);
        order.setStatus("PENDING");
        order.setRemark("预约自动生成（" + playersCount + "人）");

        boolean created = orderService.addOrder(order);
        if (!created) {
            throw new IllegalStateException("预约成功但自动生成订单失败");
        }
    }

    private void cancelPendingOrderByUserAndSession(Integer userId, Integer sessionId) {
        if (userId == null || sessionId == null) {
            return;
        }
        List<Order> orders = orderService.getByUserId(userId);
        if (orders == null || orders.isEmpty()) {
            return;
        }
        for (Order item : orders) {
            if (item == null || !sessionId.equals(item.getSessionId())) {
                continue;
            }
            String status = item.getStatus() == null ? "" : item.getStatus().trim().toUpperCase();
            if ("PENDING".equals(status)) {
                item.setStatus("CANCELLED");
                orderService.updateOrder(item);
            }
        }
    }

    private String generateOrderNo(Integer userId, Integer sessionId) {
        int suffix = ThreadLocalRandom.current().nextInt(100, 1000);
        return "ORD" + System.currentTimeMillis() + userId + sessionId + suffix;
    }
}
