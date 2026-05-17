package com.example.dm.mapper;

import com.example.dm.entity.Evaluation;
import java.util.List;

public interface EvaluationMapper {
    Evaluation selectById(Integer id);
    List<Evaluation> selectByUserId(Integer userId);
    List<Evaluation> selectBySessionId(Integer sessionId);
    List<Evaluation> selectByDmId(Integer dmId);
    List<Evaluation> selectAll();
    int insert(Evaluation evaluation);
    int update(Evaluation evaluation);
    int delete(Integer id);
    Double selectAverageRatingByDmId(Integer dmId);
}
