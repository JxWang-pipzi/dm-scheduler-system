package com.example.dm.service;

import com.example.dm.entity.Session;
import com.example.dm.vo.PageResult;
import java.util.List;
import java.util.Date;

public interface SessionService {
    Session getSessionById(Integer id);

    List<Session> getAllSessions();

    List<Session> getSessionsByDmId(Integer dmId);

    List<Session> getSessionsByTimeRange(Date startTime, Date endTime);

    /** 分页查询场次 */
    PageResult<Session> getSessionPage(String keyword, String status, String startDate, String endDate, Integer pageNum,
            Integer pageSize);

    /** 按状态统计场次数 */
    Integer countByStatus(String status);

    Session addSession(Session session);

    Session updateSession(Session session);

    boolean deleteSession(Integer id);
}