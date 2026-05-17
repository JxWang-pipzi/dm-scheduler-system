package com.example.dm.service.impl;

import com.example.dm.entity.Session;
import com.example.dm.mapper.SessionMapper;
import com.example.dm.service.SessionService;
import com.example.dm.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Date;

@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private SessionMapper sessionMapper;

    @Override
    public Session getSessionById(Integer id) {
        return sessionMapper.selectById(id);
    }

    @Override
    public List<Session> getAllSessions() {
        return sessionMapper.selectAll();
    }

    @Override
    public List<Session> getSessionsByDmId(Integer dmId) {
        return sessionMapper.selectByDmId(dmId);
    }

    @Override
    public List<Session> getSessionsByTimeRange(Date startTime, Date endTime) {
        return sessionMapper.selectByTimeRange(startTime, endTime);
    }

    @Override
    public PageResult<Session> getSessionPage(String keyword, String status, String startDate, String endDate,
            Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Session> list = sessionMapper.selectPage(keyword, status, startDate, endDate, offset, pageSize);
        Long total = sessionMapper.countByCondition(keyword, status, startDate, endDate);
        return PageResult.of(total, list, pageNum, pageSize);
    }

    @Override
    public Integer countByStatus(String status) {
        return sessionMapper.countByStatus(status);
    }

    @Override
    public Session addSession(Session session) {
        return sessionMapper.insert(session) > 0 ? sessionMapper.selectById(session.getId()) : null;
    }

    @Override
    public Session updateSession(Session session) {
        return sessionMapper.update(session) > 0 ? sessionMapper.selectById(session.getId()) : null;
    }

    @Override
    public boolean deleteSession(Integer id) {
        return sessionMapper.delete(id) > 0;
    }
}