package com.example.dm.service.impl;

import com.example.dm.entity.Evaluation;
import com.example.dm.entity.Dm;
import com.example.dm.mapper.EvaluationMapper;
import com.example.dm.mapper.DmMapper;
import com.example.dm.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private DmMapper dmMapper;

    @Override
    public Evaluation getEvaluationById(Integer id) {
        return evaluationMapper.selectById(id);
    }

    @Override
    public List<Evaluation> getEvaluationsByUserId(Integer userId) {
        return evaluationMapper.selectByUserId(userId);
    }

    @Override
    public List<Evaluation> getEvaluationsBySessionId(Integer sessionId) {
        return evaluationMapper.selectBySessionId(sessionId);
    }

    @Override
    public List<Evaluation> getEvaluationsByDmId(Integer dmId) {
        return evaluationMapper.selectByDmId(dmId);
    }

    @Override
    public List<Evaluation> getAllEvaluations() {
        return evaluationMapper.selectAll();
    }

    @Override
    @Transactional
    public boolean addEvaluation(Evaluation evaluation) {
        // 添加评价
        int result = evaluationMapper.insert(evaluation);
        if (result > 0) {
            // 更新DM评分
            updateDmRating(evaluation.getDmId());
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean updateEvaluation(Evaluation evaluation) {
        // 更新评价
        int result = evaluationMapper.update(evaluation);
        if (result > 0) {
            // 更新DM评分
            updateDmRating(evaluation.getDmId());
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteEvaluation(Integer id) {
        // 获取评价信息
        Evaluation evaluation = evaluationMapper.selectById(id);
        if (evaluation == null) {
            return false;
        }

        // 删除评价
        int result = evaluationMapper.delete(id);
        if (result > 0) {
            // 更新DM评分
            updateDmRating(evaluation.getDmId());
            return true;
        }
        return false;
    }

    @Override
    public Double getAverageRatingByDmId(Integer dmId) {
        return evaluationMapper.selectAverageRatingByDmId(dmId);
    }

    /**
     * 更新DM评分
     * @param dmId DM ID
     */
    private void updateDmRating(Integer dmId) {
        Double averageRating = evaluationMapper.selectAverageRatingByDmId(dmId);
        if (averageRating == null) {
            averageRating = 0.0;
        }

        Dm dm = dmMapper.selectById(dmId);
        if (dm != null) {
            dm.setRating(averageRating);
            dmMapper.update(dm);
        }
    }
}
