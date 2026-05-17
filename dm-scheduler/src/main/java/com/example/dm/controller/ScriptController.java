package com.example.dm.controller;

import com.example.dm.entity.Script;
import com.example.dm.service.ScriptService;
import com.example.dm.util.Result;
import com.example.dm.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/script")
public class ScriptController {

    @Autowired
    private ScriptService scriptService;

    @GetMapping("/list")
    public Result getAllScripts() {
        List<Script> scripts = scriptService.getAllScripts();
        return Result.success(scripts);
    }

    /** 分页查询剧本 */
    @GetMapping("/page")
    public Result getScriptPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty) {
        try {
            PageResult<Script> page = scriptService.getScriptPage(keyword, type, difficulty, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(5000, "获取剧本列表失败");
        }
    }

    @GetMapping("/listByType/{type}")
    public Result getScriptsByType(@PathVariable String type) {
        List<Script> scripts = scriptService.getScriptsByType(type);
        return Result.success(scripts);
    }

    @GetMapping("/detail/{id}")
    public Result getScriptById(@PathVariable Integer id) {
        Script script = scriptService.getScriptById(id);
        return script != null ? Result.success(script) : Result.error("剧本不存在");
    }

    @PostMapping("/add")
    public Result addScript(@RequestBody Script script) {
        String validationError = validateScript(script);
        if (validationError != null) {
            return Result.error(4000, validationError);
        }
        Script created = scriptService.addScript(script);
        return created != null ? Result.success(created) : Result.error(5000, "添加剧本失败");
    }

    @PutMapping("/update")
    public Result updateScript(@RequestBody Script script) {
        if (script.getId() == null)
            return Result.error(4000, "id不能为空");
        String validationError = validateScript(script);
        if (validationError != null) {
            return Result.error(4000, validationError);
        }
        Script updated = scriptService.updateScript(script);
        if (updated == null)
            return Result.error(5000, "更新剧本失败");
        return Result.success(updated);
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteScript(@PathVariable Integer id) {
        boolean success = scriptService.deleteScript(id);
        return success ? Result.success() : Result.error("删除剧本失败");
    }

    @GetMapping("/hot")
    public Result getHotScripts(@RequestParam(defaultValue = "4") Integer limit) {
        try {
            List<Script> hotScripts = scriptService.getHotScripts(limit);
            return Result.success(hotScripts);
        } catch (Exception e) {
            return Result.error(5000, "获取热门剧本失败");
        }
    }

    private String validateScript(Script script) {
        if (script == null) {
            return "请求参数不能为空";
        }
        if (script.getScriptName() == null || script.getScriptName().trim().isEmpty()) {
            return "剧本名不能为空";
        }
        if (script.getType() == null || script.getType().trim().isEmpty()) {
            return "类型不能为空";
        }
        if (script.getDifficulty() == null || script.getDifficulty().trim().isEmpty()) {
            return "难度不能为空";
        }
        if (script.getNeedDmLevel() == null || script.getNeedDmLevel() < 1) {
            return "DM等级必须大于0";
        }
        if (script.getMinPlayers() == null || script.getMinPlayers() < 1) {
            return "最少人数必须大于0";
        }
        if (script.getMaxPlayers() == null || script.getMaxPlayers() < 1) {
            return "最多人数必须大于0";
        }
        if (script.getMaxPlayers() < script.getMinPlayers()) {
            return "最多人数不能小于最少人数";
        }
        if (script.getDuration() == null || script.getDuration() < 1) {
            return "时长必须大于0";
        }
        if (script.getPrice() == null || script.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return "价格必须大于0";
        }
        return null;
    }
}
