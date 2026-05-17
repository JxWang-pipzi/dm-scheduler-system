package com.example.dm.service.impl;

import com.example.dm.entity.Dm;
import com.example.dm.mapper.DmMapper;
import com.example.dm.service.DmScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DmScheduleServiceImpl implements DmScheduleService {

    @Autowired
    private DmMapper dmMapper;

    @Override
    public List<Dm> recommendDms(Integer needDmLevel) {
        // 获取所有可用的DM
        List<Dm> availableDms = getAvailableDms();

        // 过滤出等级满足需求的DM
        List<Dm> qualifiedDms = availableDms.stream()
                .filter(dm -> dm.getDmLevel() >= needDmLevel)
                .collect(Collectors.toList());

        // 按综合评分排序
        return sortDmsByScore(qualifiedDms);
    }

    @Override
    public Double calculateScore(Dm dm) {
        // 综合评分 = 0.4 × DM等级 + 0.3 × 主持经验 + 0.3 × 用户评分
        double levelScore = dm.getDmLevel() * 0.4;
        double experienceScore = (double) dm.getExperience() * 0.3;
        double ratingScore = dm.getRating() * 0.3;

        return levelScore + experienceScore + ratingScore;
    }

    @Override
    public List<Dm> getAvailableDms() {
        // 这里应该从数据库查询所有状态为AVAILABLE的DM
        // 暂时返回所有DM，后续可以根据实际需求修改
        List<Dm> allDms = dmMapper.selectAll();
        return allDms.stream()
                .filter(dm -> "AVAILABLE".equals(dm.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Dm> sortDmsByScore(List<Dm> dms) {
        // 创建一个新的列表，避免修改原列表
        List<Dm> sortedDms = new ArrayList<>(dms);

        // 按综合评分降序排序
        sortedDms.sort((dm1, dm2) -> {
            double score1 = calculateScore(dm1);
            double score2 = calculateScore(dm2);
            return Double.compare(score2, score1); // 降序排序
        });

        return sortedDms;
    }
}
