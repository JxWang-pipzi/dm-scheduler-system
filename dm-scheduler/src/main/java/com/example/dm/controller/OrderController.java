package com.example.dm.controller;

import com.example.dm.util.Result;
import com.example.dm.util.AuthContext;
import com.example.dm.entity.Order;
import com.example.dm.entity.Session;
import com.example.dm.service.OrderService;
import com.example.dm.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PAID = "PAID";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_REFUNDED = "REFUNDED";
    private static final Set<String> ALLOWED_ORDER_STATUSES = new HashSet<String>();
    private static final Set<String> ALLOWED_PAY_METHODS = new HashSet<String>();

    static {
        ALLOWED_ORDER_STATUSES.add(STATUS_PENDING);
        ALLOWED_ORDER_STATUSES.add(STATUS_PAID);
        ALLOWED_ORDER_STATUSES.add(STATUS_COMPLETED);
        ALLOWED_ORDER_STATUSES.add(STATUS_CANCELLED);
        ALLOWED_ORDER_STATUSES.add(STATUS_REFUNDED);

        ALLOWED_PAY_METHODS.add("WECHAT");
        ALLOWED_PAY_METHODS.add("ALIPAY");
        ALLOWED_PAY_METHODS.add("CASH");
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private SessionService sessionService;

    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error(4004, "订单不存在");
        }
        Result permissionCheck = ensureOrderReadable(order);
        if (permissionCheck != null) {
            return permissionCheck;
        }
        return Result.success(order);
    }

    @GetMapping("/orderNo/{orderNo}")
    public Result getByOrderNo(@PathVariable String orderNo) {
        Order order = orderService.getByOrderNo(orderNo);
        if (order == null) {
            return Result.error(4004, "订单不存在");
        }
        Result permissionCheck = ensureOrderReadable(order);
        if (permissionCheck != null) {
            return permissionCheck;
        }
        return Result.success(order);
    }

    @GetMapping("/user/{userId}")
    public Result getByUserId(@PathVariable Integer userId) {
        Integer currentUserId = AuthContext.getUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        if (AuthContext.isUser() && !currentUserId.equals(userId)) {
            return Result.error(403, "无权查看其他用户订单");
        }
        return Result.success(orderService.getByUserId(userId));
    }

    @GetMapping("/session/{sessionId}")
    public Result getBySessionId(@PathVariable Integer sessionId) {
        List<Order> list = orderService.getBySessionId(sessionId);
        if (AuthContext.isUser()) {
            Integer currentUserId = AuthContext.getUserId();
            List<Order> ownList = new ArrayList<Order>();
            for (Order order : list) {
                if (order != null && currentUserId != null && currentUserId.equals(order.getUserId())) {
                    ownList.add(order);
                }
            }
            return Result.success(ownList);
        }
        return Result.success(list);
    }

    @GetMapping("/page")
    public Result getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        if (AuthContext.isUser()) {
            Integer currentUserId = AuthContext.getUserId();
            List<Order> list = orderService.getByUserId(currentUserId);
            List<Order> filteredList = filterOrders(list, keyword, status);
            return Result.success(toPageResult(filteredList, pageNum, pageSize));
        }

        Map<String, Object> pageResult = orderService.getPage(keyword, status, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result addOrder(@RequestBody Order order) {
        Integer currentUserId = AuthContext.getUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        if (AuthContext.isUser()) {
            return Result.error(403, "用户订单由预约自动生成，不支持手动创建");
        }
        if (order != null && (order.getOrderNo() == null || order.getOrderNo().trim().isEmpty())) {
            order.setOrderNo("ORD" + System.currentTimeMillis());
        }
        String validationError = validateOrder(order, false);
        if (validationError != null) {
            return Result.error(4000, validationError);
        }
        if (orderService.addOrder(order)) {
            return Result.success("创建订单成功");
        }
        return Result.error("创建订单失败");
    }

    @PutMapping
    public Result updateOrder(@RequestBody Order order) {
        if (order == null || order.getId() == null) {
            return Result.error(4000, "订单ID不能为空");
        }
        if (AuthContext.isUser()) {
            return Result.error(403, "普通用户无权直接修改订单");
        }
        Order existing = orderService.getById(order.getId());
        if (existing == null) {
            return Result.error(4004, "订单不存在");
        }
        Result permissionCheck = ensureOrderWritable(existing);
        if (permissionCheck != null) {
            return permissionCheck;
        }
        String transitionError = validateOrderStatusTransition(existing, order);
        if (transitionError != null) {
            return Result.error(4000, transitionError);
        }
        String validationError = validateOrder(order, true);
        if (validationError != null) {
            return Result.error(4000, validationError);
        }
        if (orderService.updateOrder(order)) {
            return Result.success("更新订单成功");
        }
        return Result.error("更新订单失败");
    }

    @PutMapping("/pay/{id}")
    public Result payOrder(@PathVariable Integer id, @RequestParam String payMethod) {
        Order existing = orderService.getById(id);
        if (existing == null) {
            return Result.error(4004, "订单不存在");
        }
        Result permissionCheck = ensureOrderReadable(existing);
        if (permissionCheck != null) {
            return permissionCheck;
        }
        if (!AuthContext.hasAnyRole("ADMIN", "USER")) {
            return Result.error(403, "无权限支付订单");
        }
        String method = payMethod == null ? "" : payMethod.trim().toUpperCase();
        if (!ALLOWED_PAY_METHODS.contains(method)) {
            return Result.error(4000, "支付方式非法");
        }

        String oldStatus = normalizeOrderStatus(existing.getStatus());
        if (!STATUS_PENDING.equals(oldStatus)) {
            return Result.error(4000, "仅待支付订单允许支付");
        }

        Session session = sessionService.getSessionById(existing.getSessionId());
        if (session == null) {
            return Result.error(4000, "关联场次不存在");
        }
        String sessionStatus = session.getStatus() == null ? "" : session.getStatus().trim().toUpperCase();
        if (!"PENDING".equals(sessionStatus)) {
            return Result.error(4000, "场次已开局或结束，无法支付");
        }
        Date now = new Date();
        if (session.getStartTime() != null && !now.before(session.getStartTime())) {
            return Result.error(4000, "已到开场时间，支付已关闭");
        }

        existing.setStatus(STATUS_PAID);
        existing.setPayMethod(method);
        existing.setPayTime(now);
        if (orderService.updateOrder(existing)) {
            return Result.success("支付成功");
        }
        return Result.error("支付失败");
    }

    @DeleteMapping("/{id}")
    public Result deleteOrder(@PathVariable Integer id) {
        if (AuthContext.isUser()) {
            return Result.error(403, "普通用户无权删除订单");
        }
        Order existing = orderService.getById(id);
        if (existing == null) {
            return Result.error(4004, "订单不存在");
        }
        Result permissionCheck = ensureOrderWritable(existing);
        if (permissionCheck != null) {
            return permissionCheck;
        }
        if (orderService.deleteOrder(id)) {
            return Result.success("删除订单成功");
        }
        return Result.error("删除订单失败");
    }

    private Result ensureOrderReadable(Order order) {
        if (!AuthContext.isUser()) {
            return null;
        }
        Integer currentUserId = AuthContext.getUserId();
        if (currentUserId == null || order == null || !currentUserId.equals(order.getUserId())) {
            return Result.error(403, "无权查看其他用户订单");
        }
        return null;
    }

    private Result ensureOrderWritable(Order order) {
        if (!AuthContext.isUser()) {
            return null;
        }
        Integer currentUserId = AuthContext.getUserId();
        if (currentUserId == null || order == null || !currentUserId.equals(order.getUserId())) {
            return Result.error(403, "无权操作其他用户订单");
        }
        return null;
    }

    private List<Order> filterOrders(List<Order> source, String keyword, String status) {
        List<Order> filtered = new ArrayList<Order>();
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase();

        if (source == null) {
            return filtered;
        }
        for (Order order : source) {
            if (order == null) {
                continue;
            }
            if (!normalizedKeyword.isEmpty()) {
                String orderNo = order.getOrderNo() == null ? "" : order.getOrderNo().toLowerCase();
                String userId = order.getUserId() == null ? "" : String.valueOf(order.getUserId());
                String sessionId = order.getSessionId() == null ? "" : String.valueOf(order.getSessionId());
                if (!orderNo.contains(normalizedKeyword)
                        && !userId.contains(normalizedKeyword)
                        && !sessionId.contains(normalizedKeyword)) {
                    continue;
                }
            }
            if (!normalizedStatus.isEmpty()) {
                String orderStatus = order.getStatus() == null ? "" : order.getStatus().trim().toUpperCase();
                if (!normalizedStatus.equals(orderStatus)) {
                    continue;
                }
            }
            filtered.add(order);
        }
        return filtered;
    }

    private Map<String, Object> toPageResult(List<Order> source, Integer pageNum, Integer pageSize) {
        int total = source == null ? 0 : source.size();
        int start = (pageNum - 1) * pageSize;
        if (start < 0) {
            start = 0;
        }
        if (start > total) {
            start = total;
        }
        int end = start + pageSize;
        if (end > total) {
            end = total;
        }

        List<Order> pageList = new ArrayList<Order>();
        if (source != null) {
            for (int i = start; i < end; i++) {
                pageList.add(source.get(i));
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("list", pageList);
        result.put("total", total);
        return result;
    }

    private String validateOrder(Order order, boolean requireId) {
        if (order == null) {
            return "请求参数不能为空";
        }
        if (requireId && order.getId() == null) {
            return "订单ID不能为空";
        }
        if (order.getOrderNo() == null || order.getOrderNo().trim().isEmpty()) {
            return "订单编号不能为空";
        }
        if (order.getUserId() == null || order.getUserId() < 1) {
            return "用户ID不能为空";
        }
        if (order.getSessionId() == null || order.getSessionId() < 1) {
            return "场次ID不能为空";
        }
        if (order.getTotalPrice() == null || order.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return "订单金额必须大于0";
        }
        if (order.getStatus() == null || order.getStatus().trim().isEmpty()) {
            return "订单状态不能为空";
        }

        String status = order.getStatus().trim().toUpperCase();
        order.setStatus(status);
        if (!ALLOWED_ORDER_STATUSES.contains(status)) {
            return "订单状态非法";
        }
        if (order.getPayMethod() != null && !order.getPayMethod().trim().isEmpty()) {
            String payMethod = order.getPayMethod().trim().toUpperCase();
            if (!ALLOWED_PAY_METHODS.contains(payMethod)) {
                return "支付方式非法";
            }
            order.setPayMethod(payMethod);
        }
        if ("PAID".equals(status) || "COMPLETED".equals(status)) {
            if (order.getPayMethod() == null || order.getPayMethod().trim().isEmpty()) {
                return "已支付或已完成订单必须填写支付方式";
            }
            if (order.getPayTime() == null) {
                return "已支付或已完成订单必须填写支付时间";
            }
        }
        return null;
    }

    private String validateOrderStatusTransition(Order existing, Order incoming) {
        if (existing == null || incoming == null) {
            return null;
        }
        String oldStatus = normalizeOrderStatus(existing.getStatus());
        String newStatus = normalizeOrderStatus(incoming.getStatus());
        if (newStatus.isEmpty()) {
            newStatus = oldStatus;
            incoming.setStatus(newStatus);
        } else {
            incoming.setStatus(newStatus);
        }
        if (oldStatus.equals(newStatus)) {
            return null;
        }
        if (STATUS_PENDING.equals(oldStatus)) {
            if (STATUS_PAID.equals(newStatus) || STATUS_CANCELLED.equals(newStatus)) {
                return null;
            }
            return "待支付订单仅允许变更为已支付或已取消";
        }
        if (STATUS_PAID.equals(oldStatus)) {
            if (STATUS_COMPLETED.equals(newStatus) || STATUS_REFUNDED.equals(newStatus)) {
                return null;
            }
            return "已支付订单仅允许变更为已完成或已退款";
        }
        if (STATUS_COMPLETED.equals(oldStatus)) {
            return "已完成订单不允许变更状态";
        }
        if (STATUS_CANCELLED.equals(oldStatus)) {
            return "已取消订单不允许变更状态";
        }
        if (STATUS_REFUNDED.equals(oldStatus)) {
            return "已退款订单不允许变更状态";
        }
        return "订单状态异常，无法变更";
    }

    private String normalizeOrderStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }
}
