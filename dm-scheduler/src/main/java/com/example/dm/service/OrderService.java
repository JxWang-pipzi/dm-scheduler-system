package com.example.dm.service;

import com.example.dm.entity.Order;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Order getById(Integer id);

    Order getByOrderNo(String orderNo);

    List<Order> getAllOrders();

    List<Order> getByUserId(Integer userId);

    List<Order> getBySessionId(Integer sessionId);

    Map<String, Object> getPage(String keyword, String status, Integer pageNum, Integer pageSize);

    boolean addOrder(Order order);

    boolean updateOrder(Order order);

    boolean deleteOrder(Integer id);
}
