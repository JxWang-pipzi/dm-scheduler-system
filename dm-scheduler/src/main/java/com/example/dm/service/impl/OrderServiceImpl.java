package com.example.dm.service.impl;

import com.example.dm.entity.Order;
import com.example.dm.mapper.OrderMapper;
import com.example.dm.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Order getById(Integer id) {
        return orderMapper.selectById(id);
    }

    @Override
    public Order getByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderMapper.selectAll();
    }

    @Override
    public List<Order> getByUserId(Integer userId) {
        return orderMapper.selectByUserId(userId);
    }

    @Override
    public List<Order> getBySessionId(Integer sessionId) {
        return orderMapper.selectBySessionId(sessionId);
    }

    @Override
    public Map<String, Object> getPage(String keyword, String status, Integer pageNum, Integer pageSize) {
        Integer offset = (pageNum - 1) * pageSize;
        List<Order> list = orderMapper.selectPage(keyword, status, offset, pageSize);
        Long total = orderMapper.countByCondition(keyword, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    public boolean addOrder(Order order) {
        return orderMapper.insert(order) > 0;
    }

    @Override
    public boolean updateOrder(Order order) {
        return orderMapper.update(order) > 0;
    }

    @Override
    public boolean deleteOrder(Integer id) {
        return orderMapper.delete(id) > 0;
    }
}
