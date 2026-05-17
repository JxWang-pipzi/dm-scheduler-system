package com.example.dm.service;

import com.example.dm.entity.Dm;
import java.util.List;

public interface DmScheduleService {
    /**
     * 根据剧本需求推荐最合适的DM
     * @param needDmLevel 剧本所需DM等级
     * @return 推荐的DM列表，按综合评分排序
     */
    List<Dm> recommendDms(Integer needDmLevel);

    /**
     * 计算DM的综合评分
     * @param dm DM对象
     * @return 综合评分
     */
    Double calculateScore(Dm dm);

    /**
     * 获取可用的DM列表
     * @return 可用的DM列表
     */
    List<Dm> getAvailableDms();

    /**
     * 根据评分排序DM列表
     * @param dms DM列表
     * @return 排序后的DM列表
     */
    List<Dm> sortDmsByScore(List<Dm> dms);
}
