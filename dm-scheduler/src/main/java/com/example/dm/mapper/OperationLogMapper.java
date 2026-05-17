package com.example.dm.mapper;

import com.example.dm.entity.OperationLog;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 操作日志Mapper
 */
public interface OperationLogMapper {
    /** 分页查询操作日志 */
    List<OperationLog> selectPage(@Param("action") String action, @Param("target") String target,
            @Param("startDate") String startDate, @Param("endDate") String endDate,
            @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    Long countByCondition(@Param("action") String action, @Param("target") String target,
            @Param("startDate") String startDate, @Param("endDate") String endDate);

    int insert(OperationLog log);

    List<OperationLog> selectAll();
}
