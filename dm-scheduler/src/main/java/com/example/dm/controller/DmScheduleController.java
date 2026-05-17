package com.example.dm.controller;

import com.example.dm.entity.Dm;
import com.example.dm.service.DmScheduleService;
import com.example.dm.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dm/schedule")
public class DmScheduleController {

    @Autowired
    private DmScheduleService dmScheduleService;

    @GetMapping("/recommend/{needDmLevel}")
    public Result recommendDms(@PathVariable Integer needDmLevel) {
        List<Dm> recommendedDms = dmScheduleService.recommendDms(needDmLevel);
        return Result.success(recommendedDms);
    }

    @GetMapping("/available")
    public Result getAvailableDms() {
        List<Dm> availableDms = dmScheduleService.getAvailableDms();
        return Result.success(availableDms);
    }

    @PostMapping("/calculate-score")
    public Result calculateScore(@RequestBody Dm dm) {
        Double score = dmScheduleService.calculateScore(dm);
        return Result.success(score);
    }
}
