package com.example.dm.controller;

import com.example.dm.entity.Dm;
import com.example.dm.service.DmScheduleService;
import com.example.dm.service.DmService;
import com.example.dm.util.Result;
import com.example.dm.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dm")
public class DmController {

    @Autowired
    private DmScheduleService dmScheduleService;

    @Autowired
    private DmService dmService;

    @GetMapping("/list")
    public Result getAllDms() {
        List<Dm> dms = dmService.getAllDms();
        return Result.success(dms);
    }

    /** 分页查询DM */
    @GetMapping("/page")
    public Result getDmPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        try {
            PageResult<Dm> page = dmService.getDmPage(keyword, status, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(5000, "获取DM列表失败");
        }
    }

    @GetMapping("/detail/{id}")
    public Result getDmById(@PathVariable Integer id) {
        Dm dm = dmService.getDmById(id);
        return dm != null ? Result.success(dm) : Result.error("DM不存在");
    }

    @PostMapping("/add")
    public Result addDm(@RequestBody Dm dm) {
        if (dm.getUserId() == null)
            return Result.error(4000, "关联用户ID不能为空");
        if (dm.getDmLevel() == null)
            dm.setDmLevel(1);
        if (dm.getExperience() == null)
            dm.setExperience(0);
        if (dm.getRating() == null)
            dm.setRating(5.0);
        if (dm.getStatus() == null || dm.getStatus().trim().isEmpty())
            dm.setStatus("AVAILABLE");
        if (dm.getSpecialty() == null)
            dm.setSpecialty("");
        if (dm.getTotalSessions() == null)
            dm.setTotalSessions(0);
        if (dm.getWeeklyMaxSessions() == null)
            dm.setWeeklyMaxSessions(10);
        if (dm.getBio() == null)
            dm.setBio("");
        Dm created = dmService.addDm(dm);
        return created != null ? Result.success(created) : Result.error(5000, "添加DM失败");
    }

    @PutMapping("/update")
    public Result updateDm(@RequestBody Dm dm) {
        if (dm.getId() == null)
            return Result.error(4000, "id不能为空");
        Dm updated = dmService.updateDm(dm);
        if (updated == null)
            return Result.error(5000, "更新DM失败");
        return Result.success(updated);
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteDm(@PathVariable Integer id) {
        boolean success = dmService.deleteDm(id);
        return success ? Result.success() : Result.error("删除DM失败");
    }

    @GetMapping("/available")
    public Result getAvailableDms() {
        List<Dm> availableDms = dmScheduleService.getAvailableDms();
        return Result.success(availableDms);
    }

    @GetMapping("/recommend/{needDmLevel}")
    public Result recommendDms(@PathVariable Integer needDmLevel) {
        List<Dm> recommendedDms = dmScheduleService.recommendDms(needDmLevel);
        return Result.success(recommendedDms);
    }

    @PostMapping("/calculate-score")
    public Result calculateScore(@RequestBody Dm dm) {
        Double score = dmScheduleService.calculateScore(dm);
        return Result.success(score);
    }
}
