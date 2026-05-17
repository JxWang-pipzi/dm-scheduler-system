package com.example.dm.service;

import com.example.dm.entity.Dm;
import com.example.dm.vo.PageResult;
import java.util.List;

public interface DmService {
    List<Dm> getAllDms();

    Dm getDmById(Integer id);

    /** 分页查询DM */
    PageResult<Dm> getDmPage(String keyword, String status, Integer pageNum, Integer pageSize);

    Dm addDm(Dm dm);

    Dm updateDm(Dm dm);

    boolean deleteDm(Integer id);
}
