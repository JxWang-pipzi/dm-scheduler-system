package com.example.dm.service.impl;

import com.example.dm.entity.Dm;
import com.example.dm.mapper.DmMapper;
import com.example.dm.service.DmService;
import com.example.dm.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DmServiceImpl implements DmService {

    @Autowired
    private DmMapper dmMapper;

    @Override
    public List<Dm> getAllDms() {
        return dmMapper.selectAll();
    }

    @Override
    public Dm getDmById(Integer id) {
        return dmMapper.selectById(id);
    }

    @Override
    public PageResult<Dm> getDmPage(String keyword, String status, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Dm> list = dmMapper.selectPage(keyword, status, offset, pageSize);
        Long total = dmMapper.countByCondition(keyword, status);
        return PageResult.of(total, list, pageNum, pageSize);
    }

    @Override
    public Dm addDm(Dm dm) {
        int result = dmMapper.insert(dm);
        if (result <= 0)
            return null;
        return dmMapper.selectById(dm.getId());
    }

    @Override
    public Dm updateDm(Dm dm) {
        int result = dmMapper.update(dm);
        if (result <= 0)
            return null;
        return dmMapper.selectById(dm.getId());
    }

    @Override
    public boolean deleteDm(Integer id) {
        int result = dmMapper.delete(id);
        return result > 0;
    }
}
