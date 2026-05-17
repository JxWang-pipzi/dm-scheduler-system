package com.example.dm.service;

import com.example.dm.entity.Evaluation;
import java.util.List;

public interface EvaluationService {
    Evaluation getEvaluationById(Integer id);
    List<Evaluation> getEvaluationsByUserId(Integer userId);
    List<Evaluation> getEvaluationsBySessionId(Integer sessionId);
    List<Evaluation> getEvaluationsByDmId(Integer dmId);
    List<Evaluation> getAllEvaluations();
    boolean addEvaluation(Evaluation evaluation);
    boolean updateEvaluation(Evaluation evaluation);
    boolean deleteEvaluation(Integer id);
    Double getAverageRatingByDmId(Integer dmId);
}
