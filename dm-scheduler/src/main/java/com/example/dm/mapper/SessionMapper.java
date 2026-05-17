package com.example.dm.mapper;

import com.example.dm.entity.Session;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Date;

public interface SessionMapper {
    Session selectById(Integer id);

    List<Session> selectAll();

    List<Session> selectByDmId(Integer dmId);

    List<Session> selectByTimeRange(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /** 分页查询（含状态筛选、日期范围筛选） */
    List<Session> selectPage(@Param("keyword") String keyword, @Param("status") String status,
            @Param("startDate") String startDate, @Param("endDate") String endDate,
            @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    /** 统计满足条件的记录数 */
    Long countByCondition(@Param("keyword") String keyword, @Param("status") String status,
            @Param("startDate") String startDate, @Param("endDate") String endDate);

    /** 统计各状态场次数量 */
    Integer countByStatus(@Param("status") String status);

    int insert(Session session);

    int update(Session session);

    int delete(Integer id);
}