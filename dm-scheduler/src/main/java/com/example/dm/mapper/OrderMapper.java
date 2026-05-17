package com.example.dm.mapper;

import com.example.dm.entity.Order;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface OrderMapper {
    Order selectById(Integer id);

    Order selectByOrderNo(String orderNo);

    List<Order> selectByUserId(Integer userId);

    List<Order> selectBySessionId(Integer sessionId);

    List<Order> selectAll();

    /** 分页查询（含关键词搜索、状态筛选） */
    List<Order> selectPage(@Param("keyword") String keyword, @Param("status") String status,
            @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    /** 统计满足条件的记录数 */
    Long countByCondition(@Param("keyword") String keyword, @Param("status") String status);

    int insert(Order order);

    int update(Order order);

    int delete(Integer id);
}