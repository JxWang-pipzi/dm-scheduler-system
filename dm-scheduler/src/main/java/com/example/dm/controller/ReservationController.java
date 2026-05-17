package com.example.dm.controller;

import com.example.dm.entity.Reservation;
import com.example.dm.entity.Session;
import com.example.dm.entity.Order;
import com.example.dm.service.OrderService;
import com.example.dm.service.ReservationService;
import com.example.dm.service.SessionService;
import com.example.dm.service.SystemRuntimeConfigService;
import com.example.dm.util.AuthContext;
import com.example.dm.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final Set<String> ALLOWED_RESERVATION_STATUSES = new HashSet<String>();

    static {
        ALLOWED_RESERVATION_STATUSES.add(STATUS_CONFIRMED);
        ALLOWED_RESERVATION_STATUSES.add(STATUS_CANCELLED);
    }

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SystemRuntimeConfigService systemRuntimeConfigService;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public Result getAllReservations() {
        if (AuthContext.isUser()) {
            Integer currentUserId = AuthContext.getUserId();
            if (currentUserId == null) {
                return Result.error(401, "请先登录");
            }
            return Result.success(reservationService.getReservationsByUserId(currentUserId));
        }
        List<Reservation> reservations = reservationService.getAllReservations();
        return Result.success(reservations);
    }

    @GetMapping("/user/{userId}")
    public Result getReservationsByUserId(@PathVariable Integer userId) {
        Integer currentUserId = AuthContext.getUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        if (AuthContext.isUser() && !currentUserId.equals(userId)) {
            return Result.error(403, "无权查看其他用户预约");
        }
        List<Reservation> reservations = reservationService.getReservationsByUserId(userId);
        return Result.success(reservations);
    }

    @GetMapping("/session/{sessionId}")
    public Result getReservationsBySessionId(@PathVariable Integer sessionId) {
        List<Reservation> reservations = reservationService.getReservationsBySessionId(sessionId);
        if (AuthContext.isUser()) {
            Integer currentUserId = AuthContext.getUserId();
            if (currentUserId == null) {
                return Result.error(401, "请先登录");
            }
            java.util.List<Reservation> ownList = new java.util.ArrayList<Reservation>();
            for (Reservation item : reservations) {
                if (item != null && currentUserId.equals(item.getUserId())) {
                    ownList.add(item);
                }
            }
            return Result.success(ownList);
        }
        return Result.success(reservations);
    }

    @GetMapping("/{id}")
    public Result getReservationById(@PathVariable Integer id) {
        Reservation reservation = reservationService.getReservationById(id);
        if (reservation == null) {
            return Result.error(4004, "预约不存在");
        }
        Result permissionCheck = ensureOwnerPermission(reservation);
        if (permissionCheck != null) {
            return permissionCheck;
        }
        return Result.success(reservation);
    }

    @PostMapping
    public Result addReservation(@RequestBody Reservation reservation) {
        try {
            if (reservation == null) {
                return Result.error(4000, "请求参数不能为空");
            }
            Integer currentUserId = AuthContext.getUserId();
            if (currentUserId == null) {
                return Result.error(401, "请先登录");
            }
            if (AuthContext.isUser()) {
                reservation.setUserId(currentUserId);
            }
            if (reservation.getUserId() == null || reservation.getUserId() < 1) {
                return Result.error(4000, "用户ID不能为空");
            }
            if (reservation.getSessionId() == null || reservation.getSessionId() < 1) {
                return Result.error(4000, "场次ID不能为空");
            }
            if (reservation.getPlayersCount() == null) {
                reservation.setPlayersCount(1);
            }
            if (reservation.getPlayersCount() < 1) {
                return Result.error(4000, "预约人数必须大于0");
            }
            String leadTimeError = validateReservationLeadTime(reservation.getSessionId());
            if (leadTimeError != null) {
                return Result.error(4000, leadTimeError);
            }
            if (reservation.getStatus() == null || reservation.getStatus().trim().isEmpty()) {
                reservation.setStatus(STATUS_CONFIRMED);
            }
            String createStatus = normalizeReservationStatus(reservation.getStatus());
            if (!STATUS_CONFIRMED.equals(createStatus)) {
                return Result.error(4000, "新建预约状态只能为已确认");
            }
            reservation.setStatus(createStatus);
            boolean result = reservationService.addReservation(reservation);
            return result ? Result.success(true) : Result.error(5000, "预约失败，请稍后重试");
        } catch (IllegalStateException ex) {
            return Result.error(4000, ex.getMessage());
        } catch (Exception ex) {
            return Result.error(5000, "预约失败，请稍后重试");
        }
    }

    @PutMapping
    public Result updateReservation(@RequestBody Reservation reservation) {
        if (reservation == null || reservation.getId() == null) {
            return Result.error(4000, "预约ID不能为空");
        }
        Reservation existing = reservationService.getReservationById(reservation.getId());
        if (existing == null) {
            return Result.error(4004, "预约不存在");
        }
        Result permissionCheck = ensureOwnerPermission(existing);
        if (permissionCheck != null) {
            return permissionCheck;
        }
        String paidLockError = validateReservationPaidLock(existing);
        if (paidLockError != null) {
            return Result.error(4000, paidLockError);
        }
        if (AuthContext.isUser()) {
            reservation.setUserId(existing.getUserId());
        }
        if (reservation.getPlayersCount() == null) {
            reservation.setPlayersCount(existing.getPlayersCount());
        }
        if (reservation.getPlayersCount() == null || reservation.getPlayersCount() < 1) {
            return Result.error(4000, "预约人数必须大于0");
        }
        String oldStatus = normalizeReservationStatus(existing.getStatus());
        String newStatus = normalizeReservationStatus(reservation.getStatus());
        if (newStatus.isEmpty()) {
            newStatus = oldStatus;
            reservation.setStatus(newStatus);
        }
        if (!isAllowedReservationStatus(newStatus)) {
            return Result.error(4000, "预约状态非法");
        }
        if (STATUS_CANCELLED.equals(oldStatus)) {
            return Result.error(4000, "已取消预约不允许编辑");
        }
        if (!oldStatus.equals(newStatus)) {
            return Result.error(4000, "预约状态不可直接修改，请使用取消预约操作");
        }

        boolean result = reservationService.updateReservation(reservation);
        return result ? Result.success(true) : Result.error(5000, "更新预约失败");
    }

    @DeleteMapping("/{id}")
    public Result deleteReservation(@PathVariable Integer id) {
        Reservation existing = reservationService.getReservationById(id);
        if (existing == null) {
            return Result.error(4004, "预约不存在");
        }
        Result permissionCheck = ensureOwnerPermission(existing);
        if (permissionCheck != null) {
            return permissionCheck;
        }
        String paidLockError = validateReservationPaidLock(existing);
        if (paidLockError != null) {
            return Result.error(4000, paidLockError);
        }
        boolean result = reservationService.deleteReservation(id);
        return result ? Result.success(true) : Result.error(5000, "删除预约失败");
    }

    @PutMapping("/cancel/{id}")
    public Result cancelReservation(@PathVariable Integer id) {
        Reservation existing = reservationService.getReservationById(id);
        if (existing == null) {
            return Result.error(4004, "预约不存在");
        }
        Result permissionCheck = ensureOwnerPermission(existing);
        if (permissionCheck != null) {
            return permissionCheck;
        }
        String paidLockError = validateReservationPaidLock(existing);
        if (paidLockError != null) {
            return Result.error(4000, paidLockError);
        }
        if (STATUS_CANCELLED.equals(normalizeReservationStatus(existing.getStatus()))) {
            return Result.error(4000, "预约已是取消状态");
        }
        boolean result = reservationService.cancelReservation(id);
        return result ? Result.success(true) : Result.error(5000, "取消预约失败");
    }

    @GetMapping("/count/{sessionId}")
    public Result countConfirmedReservations(@PathVariable Integer sessionId) {
        int count = reservationService.countConfirmedReservationsBySessionId(sessionId);
        return Result.success(count);
    }

    private Result ensureOwnerPermission(Reservation reservation) {
        if (!AuthContext.isUser()) {
            return null;
        }
        Integer currentUserId = AuthContext.getUserId();
        if (currentUserId == null || reservation == null || !currentUserId.equals(reservation.getUserId())) {
            return Result.error(403, "无权操作其他用户预约");
        }
        return null;
    }

    private String validateReservationLeadTime(Integer sessionId) {
        Session session = sessionService.getSessionById(sessionId);
        if (session == null) {
            return "场次不存在";
        }
        if (session.getStartTime() == null) {
            return "场次开始时间无效";
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startAt = toLocalDateTime(session.getStartTime());
        if (startAt == null) {
            return "场次开始时间无效";
        }
        if (!startAt.isAfter(now)) {
            return "场次已到开场时间，无法预约（开场前需完成支付）";
        }
        int leadHours = systemRuntimeConfigService.getReservationLeadHours();
        if (leadHours <= 0) {
            return null;
        }
        if (startAt.isBefore(now.plusHours(leadHours))) {
            return "预约需至少提前 " + leadHours + " 小时";
        }
        return null;
    }

    private String validateReservationPaidLock(Reservation reservation) {
        if (reservation == null || reservation.getUserId() == null || reservation.getSessionId() == null) {
            return null;
        }
        List<Order> orders = orderService.getByUserId(reservation.getUserId());
        if (orders == null || orders.isEmpty()) {
            return null;
        }
        for (Order order : orders) {
            if (order == null || order.getSessionId() == null) {
                continue;
            }
            if (!reservation.getSessionId().equals(order.getSessionId())) {
                continue;
            }
            String status = order.getStatus() == null ? "" : order.getStatus().trim().toUpperCase();
            if ("PAID".equals(status) || "COMPLETED".equals(status)) {
                return "该预约已支付，不能取消或删除。如需取消请先联系管理员退款。";
            }
        }
        return null;
    }

    private LocalDateTime toLocalDateTime(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private boolean isAllowedReservationStatus(String status) {
        return status != null && ALLOWED_RESERVATION_STATUSES.contains(status);
    }

    private String normalizeReservationStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }
}
