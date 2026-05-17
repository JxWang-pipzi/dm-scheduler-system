package com.example.dm.controller;

import com.example.dm.entity.DmSchedule;
import com.example.dm.entity.Dm;
import com.example.dm.mapper.DmScheduleMapper;
import com.example.dm.mapper.DmMapper;
import com.example.dm.util.Result;
import com.example.dm.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * DM排班管理Controller - 提供排班CRUD和分页查询
 */
@RestController
@RequestMapping("/api/dm-schedule")
public class DmScheduleManageController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] DEFAULT_TIME_SLOTS = new String[] { "上午", "下午", "晚上" };

    @Autowired
    private DmScheduleMapper dmScheduleMapper;

    @Autowired
    private DmMapper dmMapper;

    /** 分页查询排班 */
    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status) {
        try {
            int offset = (pageNum - 1) * pageSize;
            List<DmSchedule> list = dmScheduleMapper.selectPage(null, date, date, status, offset, pageSize);
            Long total = dmScheduleMapper.countByCondition(null, date, date, status);
            PageResult<DmSchedule> page = PageResult.of(total, list, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(5000, "获取排班列表失败: " + e.getMessage());
        }
    }

    /** 根据DM和日期查询排班 */
    @GetMapping("/by-dm/{dmId}")
    public Result getByDmAndDate(@PathVariable Integer dmId,
            @RequestParam(required = false) String date) {
        try {
            List<DmSchedule> list = dmScheduleMapper.selectByDmIdAndDate(dmId, date);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(5000, "查询排班失败");
        }
    }

    /** 根据日期查询所有排班 */
    @GetMapping("/by-date")
    public Result getByDate(@RequestParam String date) {
        try {
            List<DmSchedule> list = dmScheduleMapper.selectByDate(date);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(5000, "查询排班失败");
        }
    }

    /** 新增排班 */
    @PostMapping("/add")
    public Result add(@RequestBody DmSchedule schedule) {
        try {
            if (schedule.getDmId() == null)
                return Result.error(4000, "DM ID不能为空");
            if (schedule.getScheduleDate() == null)
                return Result.error(4000, "排班日期不能为空");
            int affected = dmScheduleMapper.insert(schedule);
            if (affected > 0) {
                return Result.success(dmScheduleMapper.selectById(schedule.getId()));
            }
            return Result.error(5000, "添加排班失败");
        } catch (Exception e) {
            return Result.error(5000, "添加排班失败");
        }
    }

    /** 更新排班 */
    @PutMapping("/update")
    public Result update(@RequestBody DmSchedule schedule) {
        try {
            if (schedule.getId() == null)
                return Result.error(4000, "ID不能为空");
            int affected = dmScheduleMapper.update(schedule);
            if (affected > 0) {
                return Result.success(dmScheduleMapper.selectById(schedule.getId()));
            }
            return Result.error(5000, "更新排班失败");
        } catch (Exception e) {
            return Result.error(5000, "更新排班失败");
        }
    }

    /** 删除排班 */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        try {
            int affected = dmScheduleMapper.delete(id);
            return affected > 0 ? Result.success() : Result.error("删除排班失败");
        } catch (Exception e) {
            return Result.error(5000, "删除排班失败");
        }
    }

    /** 自动生成下周排班（不关联场次） */
    @PostMapping("/auto-generate-next-week")
    public Result autoGenerateNextWeek() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            LocalDate nextSunday = nextMonday.plusDays(6);
            String startDate = nextMonday.format(DATE_FORMATTER);
            String endDate = nextSunday.format(DATE_FORMATTER);

            Long existingCount = dmScheduleMapper.countByCondition(null, startDate, endDate, null);
            if (existingCount != null && existingCount > 0) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("alreadyGenerated", true);
                data.put("weekStart", nextMonday.atStartOfDay().format(DATETIME_FORMATTER));
                data.put("weekEnd", nextSunday.atTime(23, 59, 59).format(DATETIME_FORMATTER));
                data.put("existingCount", existingCount);
                return Result.success(data);
            }

            List<Dm> allDms = dmMapper.selectAll();
            if (allDms == null) {
                allDms = new ArrayList<Dm>();
            }

            int generatedCount = 0;
            for (Dm dm : allDms) {
                if (dm == null || dm.getId() == null) {
                    continue;
                }
                for (int i = 0; i < 7; i++) {
                    LocalDate date = nextMonday.plusDays(i);
                    Date scheduleDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    for (String slot : DEFAULT_TIME_SLOTS) {
                        DmSchedule schedule = new DmSchedule();
                        schedule.setDmId(dm.getId());
                        schedule.setScheduleDate(scheduleDate);
                        schedule.setTimeSlot(slot);
                        schedule.setSessionId(null);
                        schedule.setStatus("AVAILABLE");
                        generatedCount += dmScheduleMapper.insert(schedule);
                    }
                }
            }

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("alreadyGenerated", false);
            data.put("weekStart", nextMonday.atStartOfDay().format(DATETIME_FORMATTER));
            data.put("weekEnd", nextSunday.atTime(23, 59, 59).format(DATETIME_FORMATTER));
            data.put("dmCount", allDms.size());
            data.put("generatedCount", generatedCount);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(5000, "自动生成下周排班失败");
        }
    }
}
