package com.example.dm.controller;

import com.example.dm.entity.Evaluation;
import com.example.dm.service.EvaluationService;
import com.example.dm.util.AuthContext;
import com.example.dm.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @GetMapping
    public Result getAllEvaluations() {
        if (AuthContext.isUser()) {
            Integer currentUserId = AuthContext.getUserId();
            if (currentUserId == null) {
                return Result.error(401, "请先登录");
            }
            return Result.success(evaluationService.getEvaluationsByUserId(currentUserId));
        }
        List<Evaluation> evaluations = evaluationService.getAllEvaluations();
        return Result.success(evaluations);
    }

    @GetMapping("/user/{userId}")
    public Result getEvaluationsByUserId(@PathVariable Integer userId) {
        Integer currentUserId = AuthContext.getUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        if (AuthContext.isUser() && !currentUserId.equals(userId)) {
            return Result.error(403, "无权查看其他用户评价");
        }
        List<Evaluation> evaluations = evaluationService.getEvaluationsByUserId(userId);
        return Result.success(evaluations);
    }

    @GetMapping("/session/{sessionId}")
    public Result getEvaluationsBySessionId(@PathVariable Integer sessionId) {
        List<Evaluation> evaluations = evaluationService.getEvaluationsBySessionId(sessionId);
        return Result.success(evaluations);
    }

    @GetMapping("/dm/{dmId}")
    public Result getEvaluationsByDmId(@PathVariable Integer dmId) {
        List<Evaluation> evaluations = evaluationService.getEvaluationsByDmId(dmId);
        return Result.success(evaluations);
    }

    @GetMapping("/{id}")
    public Result getEvaluationById(@PathVariable Integer id) {
        Evaluation evaluation = evaluationService.getEvaluationById(id);
        if (evaluation == null) {
            return Result.error(4004, "评价不存在");
        }
        Result permissionCheck = ensureEvaluationOwner(evaluation);
        if (permissionCheck != null) {
            return permissionCheck;
        }
        return Result.success(evaluation);
    }

    @PostMapping
    public Result addEvaluation(@RequestBody Evaluation evaluation) {
        Integer currentUserId = AuthContext.getUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        if (AuthContext.isUser()) {
            evaluation.setUserId(currentUserId);
        }
        String validationError = validateEvaluation(evaluation, false);
        if (validationError != null) {
            return Result.error(4000, validationError);
        }
        boolean result = evaluationService.addEvaluation(evaluation);
        return result ? Result.success(true) : Result.error(5000, "新增评价失败");
    }

    @PutMapping
    public Result updateEvaluation(@RequestBody Evaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) {
            return Result.error(4000, "评价ID不能为空");
        }
        Evaluation existing = evaluationService.getEvaluationById(evaluation.getId());
        if (existing == null) {
            return Result.error(4004, "评价不存在");
        }
        Result permissionCheck = ensureEvaluationOwner(existing);
        if (permissionCheck != null) {
            return permissionCheck;
        }
        if (AuthContext.isUser()) {
            evaluation.setUserId(existing.getUserId());
        }
        if (evaluation.getDmId() == null) {
            evaluation.setDmId(existing.getDmId());
        }
        if (evaluation.getSessionId() == null) {
            evaluation.setSessionId(existing.getSessionId());
        }
        String validationError = validateEvaluation(evaluation, true);
        if (validationError != null) {
            return Result.error(4000, validationError);
        }
        boolean result = evaluationService.updateEvaluation(evaluation);
        return result ? Result.success(true) : Result.error(5000, "更新评价失败");
    }

    @DeleteMapping("/{id}")
    public Result deleteEvaluation(@PathVariable Integer id) {
        Evaluation existing = evaluationService.getEvaluationById(id);
        if (existing == null) {
            return Result.error(4004, "评价不存在");
        }
        Result permissionCheck = ensureEvaluationOwner(existing);
        if (permissionCheck != null) {
            return permissionCheck;
        }
        boolean result = evaluationService.deleteEvaluation(id);
        return result ? Result.success(true) : Result.error(5000, "删除评价失败");
    }

    @GetMapping("/rating/dm/{dmId}")
    public Result getAverageRatingByDmId(@PathVariable Integer dmId) {
        Double rating = evaluationService.getAverageRatingByDmId(dmId);
        return Result.success(rating);
    }

    private Result ensureEvaluationOwner(Evaluation evaluation) {
        if (!AuthContext.isUser()) {
            return null;
        }
        Integer currentUserId = AuthContext.getUserId();
        if (currentUserId == null || evaluation == null || !currentUserId.equals(evaluation.getUserId())) {
            return Result.error(403, "无权操作其他用户评价");
        }
        return null;
    }

    private String validateEvaluation(Evaluation evaluation, boolean requireId) {
        if (evaluation == null) {
            return "请求参数不能为空";
        }
        if (requireId && evaluation.getId() == null) {
            return "评价ID不能为空";
        }
        if (evaluation.getUserId() == null || evaluation.getUserId() < 1) {
            return "用户ID不能为空";
        }
        if (evaluation.getSessionId() == null || evaluation.getSessionId() < 1) {
            return "场次ID不能为空";
        }
        if (evaluation.getDmId() == null || evaluation.getDmId() < 1) {
            return "DM ID不能为空";
        }
        if (evaluation.getRating() == null || evaluation.getRating() < 1 || evaluation.getRating() > 5) {
            return "评分必须在1到5之间";
        }
        return null;
    }
}
