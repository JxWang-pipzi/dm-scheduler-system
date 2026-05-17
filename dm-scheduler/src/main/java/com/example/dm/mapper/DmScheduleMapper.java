package com.example.dm.mapper;

import com.example.dm.entity.DmSchedule;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * DM排班Mapper
 */
public interface DmScheduleMapper {
    DmSchedule selectById(Integer id);

    DmSchedule selectBySessionId(@Param("sessionId") Integer sessionId);

    /** 查询指定DM在指定日期的排班 */
    List<DmSchedule> selectByDmIdAndDate(@Param("dmId") Integer dmId, @Param("scheduleDate") String scheduleDate);

    /** 查询指定日期的所有排班 */
    List<DmSchedule> selectByDate(@Param("scheduleDate") String scheduleDate);

    /** 分页查询 */
    List<DmSchedule> selectPage(@Param("dmId") Integer dmId, @Param("startDate") String startDate,
            @Param("endDate") String endDate, @Param("status") String status,
            @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    Long countByCondition(@Param("dmId") Integer dmId, @Param("startDate") String startDate,
            @Param("endDate") String endDate, @Param("status") String status);

    List<DmSchedule> selectAll();

    int insert(DmSchedule schedule);

    int update(DmSchedule schedule);

    int delete(Integer id);

    int deleteBySessionId(@Param("sessionId") Integer sessionId);

    int releaseBySessionId(@Param("sessionId") Integer sessionId);
}
