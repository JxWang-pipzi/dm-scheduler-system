package com.example.dm.controller;

import com.example.dm.entity.Session;
import com.example.dm.entity.Script;
import com.example.dm.entity.Reservation;
import com.example.dm.entity.Order;
import com.example.dm.entity.Dm;
import com.example.dm.entity.DmSchedule;
import com.example.dm.mapper.DmScheduleMapper;
import com.example.dm.service.DmScheduleService;
import com.example.dm.service.OrderService;
import com.example.dm.service.ReservationService;
import com.example.dm.service.ScriptService;
import com.example.dm.service.SessionService;
import com.example.dm.service.SystemRuntimeConfigService;
import com.example.dm.util.AuthContext;
import com.example.dm.util.Result;
import com.example.dm.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/session")
public class SessionController {
    private static final DateTimeFormatter RESPONSE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ONGOING = "ONGOING";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String SCHEDULE_STATUS_AVAILABLE = "AVAILABLE";
    private static final String SCHEDULE_STATUS_ASSIGNED = "ASSIGNED";
    private static final Set<String> ALLOWED_SESSION_STATUSES = new HashSet<String>();

    static {
        ALLOWED_SESSION_STATUSES.add(STATUS_PENDING);
        ALLOWED_SESSION_STATUSES.add(STATUS_ONGOING);
        ALLOWED_SESSION_STATUSES.add(STATUS_COMPLETED);
        ALLOWED_SESSION_STATUSES.add(STATUS_CANCELLED);
    }

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SystemRuntimeConfigService systemRuntimeConfigService;

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private DmScheduleService dmScheduleService;

    @Autowired
    private DmScheduleMapper dmScheduleMapper;

    @GetMapping("/list")
    public Result getAllSessions() {
        List<Session> sessions = sessionService.getAllSessions();
        return Result.success(sessions);
    }

    /** 分页查询场次 */
    @GetMapping("/page")
    public Result getSessionPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            PageResult<Session> page = sessionService.getSessionPage(keyword, status, startDate, endDate, pageNum,
                    pageSize);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(5000, "获取场次列表失败");
        }
    }

    @GetMapping("/listByDm/{dmId}")
    public Result getSessionsByDmId(@PathVariable Integer dmId) {
        List<Session> sessions = sessionService.getSessionsByDmId(dmId);
        return Result.success(sessions);
    }

    @GetMapping("/detail/{id}")
    public Result getSessionById(@PathVariable Integer id) {
        Session session = sessionService.getSessionById(id);
        return session != null ? Result.success(session) : Result.error("场次不存在");
    }

    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public Result addSession(@RequestBody Session session) {
        try {
            if (session.getStatus() == null || session.getStatus().trim().isEmpty()) {
                session.setStatus(STATUS_PENDING);
            }
            if (session.getMaxPlayers() == null) {
                session.setMaxPlayers(systemRuntimeConfigService.getDefaultSessionMaxPlayers());
            }
            if (session.getCurrentPlayers() == null) {
                session.setCurrentPlayers(0);
            }
            String addStatusError = validateStatusOnCreate(session);
            if (addStatusError != null) {
                return Result.error(4000, addStatusError);
            }
            String validationError = validateSession(session, false);
            if (validationError != null) {
                return Result.error(4000, validationError);
            }
            validateDmScheduleSyncConstraints(session);
            Session created = sessionService.addSession(session);
            if (created == null) {
                return Result.error(5000, "添加场次失败");
            }
            syncDmScheduleWithSession(created, false);
            return Result.success(sessionService.getSessionById(created.getId()));
        } catch (IllegalStateException ex) {
            return Result.error(4000, ex.getMessage());
        } catch (Exception ex) {
            return Result.error(5000, "添加场次失败");
        }
    }

    @PutMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public Result updateSession(@RequestBody Session session) {
        if (session.getId() == null)
            return Result.error(4000, "id不能为空");
        Session existing = sessionService.getSessionById(session.getId());
        if (existing == null) {
            return Result.error(4004, "场次不存在");
        }

        if (session.getStatus() == null || session.getStatus().trim().isEmpty()) {
            session.setStatus(existing.getStatus());
        }
        if (session.getMaxPlayers() == null) {
            session.setMaxPlayers(
                    existing.getMaxPlayers() == null ? systemRuntimeConfigService.getDefaultSessionMaxPlayers()
                            : existing.getMaxPlayers());
        }
        if (session.getCurrentPlayers() == null) {
            session.setCurrentPlayers(existing.getCurrentPlayers() == null ? 0 : existing.getCurrentPlayers());
        }

        String updateStatusError = validateStatusOnUpdate(existing, session);
        if (updateStatusError != null) {
            return Result.error(4000, updateStatusError);
        }

        String validationError = validateSession(session, true);
        if (validationError != null) {
            return Result.error(4000, validationError);
        }
        try {
            validateDmScheduleSyncConstraints(session);
            Session updated = sessionService.updateSession(session);
            if (updated == null) {
                return Result.error(5000, "更新场次失败");
            }
            syncDmScheduleWithSession(updated, false);
            return Result.success(sessionService.getSessionById(updated.getId()));
        } catch (IllegalStateException ex) {
            return Result.error(4000, ex.getMessage());
        } catch (Exception ex) {
            return Result.error(5000, "更新场次失败");
        }
    }

    @PutMapping("/start/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result startSession(@PathVariable Integer id) {
        try {
            Session existing = sessionService.getSessionById(id);
            if (existing == null) {
                return Result.error(4004, "场次不存在");
            }
            String currentStatus = normalizeSessionStatus(existing.getStatus());
            if (!STATUS_PENDING.equals(currentStatus)) {
                return Result.error(4000, "仅等待中场次允许开局");
            }
            // 兼容历史库的 ENUM 定义（PENDING/ONGOING/COMPLETED/CANCELLED）
            existing.setStatus(STATUS_ONGOING);
            String validationError = validateSession(existing, true);
            if (validationError != null) {
                return Result.error(4000, validationError);
            }
            Session updated = sessionService.updateSession(existing);
            if (updated == null) {
                return Result.error(5000, "开局失败");
            }
            syncDmScheduleWithSession(updated, false);
            return Result.success(updated);
        } catch (Exception ex) {
            return Result.error(5000, "开局失败，请检查场次状态配置");
        }
    }

    @PutMapping("/complete/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result completeSession(@PathVariable Integer id) {
        Session existing = sessionService.getSessionById(id);
        if (existing == null) {
            return Result.error(4004, "场次不存在");
        }
        String currentStatus = normalizeSessionStatus(existing.getStatus());
        if (!STATUS_ONGOING.equals(currentStatus)) {
            return Result.error(4000, "仅进行中场次允许完场");
        }
        existing.setStatus(STATUS_COMPLETED);
        String validationError = validateSession(existing, true);
        if (validationError != null) {
            return Result.error(4000, validationError);
        }
        Session updated = sessionService.updateSession(existing);
        if (updated == null) {
            return Result.error(5000, "完场失败");
        }
        syncDmScheduleWithSession(updated, false);
        return Result.success(updated);
    }

    @DeleteMapping("/delete/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result deleteSession(@PathVariable Integer id) {
        Session existing = sessionService.getSessionById(id);
        if (existing == null) {
            return Result.error(4004, "场次不存在");
        }
        String currentStatus = normalizeSessionStatus(existing.getStatus());
        if (STATUS_ONGOING.equals(currentStatus) || STATUS_COMPLETED.equals(currentStatus)) {
            return Result.error(4000, "进行中或已完成场次不允许删除");
        }
        syncDmScheduleWithSession(existing, true);
        boolean success = sessionService.deleteSession(id);
        return success ? Result.success() : Result.error("删除场次失败");
    }

    @PostMapping("/auto-assign-next-week")
    @Transactional(rollbackFor = Exception.class)
    public Result autoAssignNextWeek() {
        if (!AuthContext.hasRole("ADMIN")) {
            return Result.error(403, "仅管理员可执行智能调度");
        }
        try {
            LocalDate today = LocalDate.now();
            LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            LocalDate nextSunday = nextMonday.plusDays(6);
            LocalDateTime weekStart = nextMonday.atStartOfDay();
            LocalDateTime weekEnd = nextSunday.atTime(23, 59, 59);
            Date rangeStart = Date.from(weekStart.atZone(ZoneId.systemDefault()).toInstant());
            Date rangeEnd = Date.from(weekEnd.atZone(ZoneId.systemDefault()).toInstant());

            List<Session> existingWeekSessions = sessionService.getSessionsByTimeRange(rangeStart, rangeEnd);
            if (existingWeekSessions != null && !existingWeekSessions.isEmpty()) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("alreadyGenerated", true);
                data.put("weekStart", weekStart.format(RESPONSE_DATETIME_FORMATTER));
                data.put("weekEnd", weekEnd.format(RESPONSE_DATETIME_FORMATTER));
                data.put("existingSessionCount", existingWeekSessions.size());
                return Result.success(data);
            }

            List<Script> scripts = scriptService.getAllScripts();
            if (scripts == null || scripts.isEmpty()) {
                return Result.error(4000, "无可用剧本，无法自动生成下周场次");
            }
            scripts.sort(Comparator.comparing(s -> s == null || s.getId() == null ? Integer.MAX_VALUE : s.getId()));

            List<Session> allSessions = sessionService.getAllSessions();
            if (allSessions == null) {
                allSessions = new ArrayList<Session>();
            }
            Map<Integer, Integer> weeklyLoadMap = buildWeeklyLoadMap(allSessions, rangeStart, rangeEnd);
            List<Map<String, Object>> created = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> skipped = new ArrayList<Map<String, Object>>();

            int createdSessionCount = 0;
            int createdScheduleCount = 0;
            for (int i = 0; i < 7; i++) {
                LocalDate day = nextMonday.plusDays(i);
                Script script = scripts.get(i % scripts.size());
                if (script == null || script.getId() == null) {
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("date", day.toString());
                    item.put("reason", "剧本数据无效");
                    skipped.add(item);
                    continue;
                }

                LocalDateTime startAt = day.atTime(20, 0, 0);
                int durationMinutes = normalizeScriptDuration(script.getDuration());
                LocalDateTime endAt = startAt.plusMinutes(durationMinutes);
                Date startTime = Date.from(startAt.atZone(ZoneId.systemDefault()).toInstant());
                Date endTime = Date.from(endAt.atZone(ZoneId.systemDefault()).toInstant());

                String businessError = validateBusinessTime(startTime, endTime);
                if (businessError != null) {
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("date", day.toString());
                    item.put("scriptId", script.getId());
                    item.put("scriptName", script.getScriptName());
                    item.put("reason", businessError);
                    skipped.add(item);
                    continue;
                }

                int needLevel = script.getNeedDmLevel() == null || script.getNeedDmLevel() < 1 ? 1 : script.getNeedDmLevel();
                List<Dm> recommendedDms = dmScheduleService.recommendDms(needLevel);
                Dm selectedDm = pickBestDmForAutoGeneration(recommendedDms, weeklyLoadMap, allSessions, startTime, endTime);
                if (selectedDm == null) {
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("date", day.toString());
                    item.put("scriptId", script.getId());
                    item.put("scriptName", script.getScriptName());
                    item.put("reason", "无可用DM（等级/排班冲突/周上限）");
                    skipped.add(item);
                    continue;
                }

                Session newSession = new Session();
                newSession.setScriptId(script.getId());
                newSession.setDmId(selectedDm.getId());
                newSession.setStartTime(startTime);
                newSession.setEndTime(endTime);
                newSession.setStatus(STATUS_PENDING);
                int maxPlayers = script.getMaxPlayers() == null || script.getMaxPlayers() < 1
                        ? systemRuntimeConfigService.getDefaultSessionMaxPlayers()
                        : script.getMaxPlayers();
                int minPlayers = script.getMinPlayers() == null || script.getMinPlayers() < 1 ? 1 : script.getMinPlayers();
                if (maxPlayers < minPlayers) {
                    maxPlayers = minPlayers;
                }
                newSession.setMaxPlayers(maxPlayers);
                newSession.setCurrentPlayers(0);
                validateDmScheduleSyncConstraints(newSession);

                Session createdSession = sessionService.addSession(newSession);
                if (createdSession == null) {
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("date", day.toString());
                    item.put("scriptId", script.getId());
                    item.put("scriptName", script.getScriptName());
                    item.put("reason", "场次创建失败");
                    skipped.add(item);
                    continue;
                }

                syncDmScheduleWithSession(createdSession, false);
                createdSessionCount++;
                createdScheduleCount++;
                allSessions.add(createdSession);
                weeklyLoadMap.put(selectedDm.getId(), weeklyLoadMap.getOrDefault(selectedDm.getId(), 0) + 1);

                Map<String, Object> item = new HashMap<String, Object>();
                item.put("sessionId", createdSession.getId());
                item.put("date", day.toString());
                item.put("scriptId", script.getId());
                item.put("scriptName", script.getScriptName());
                item.put("dmId", selectedDm.getId());
                item.put("dmLevel", selectedDm.getDmLevel());
                created.add(item);
            }

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("alreadyGenerated", false);
            data.put("weekStart", weekStart.format(RESPONSE_DATETIME_FORMATTER));
            data.put("weekEnd", weekEnd.format(RESPONSE_DATETIME_FORMATTER));
            data.put("createdSessionCount", createdSessionCount);
            data.put("createdScheduleCount", createdScheduleCount);
            data.put("skippedCount", skipped.size());
            data.put("created", created);
            data.put("skipped", skipped);
            return Result.success(data);
        } catch (IllegalStateException ex) {
            return Result.error(4000, ex.getMessage());
        } catch (Exception ex) {
            return Result.error(5000, "智能调度失败");
        }
    }

    private Map<Integer, Integer> buildWeeklyLoadMap(List<Session> sessions, Date rangeStart, Date rangeEnd) {
        Map<Integer, Integer> loadMap = new HashMap<Integer, Integer>();
        if (sessions == null || sessions.isEmpty()) {
            return loadMap;
        }
        for (Session item : sessions) {
            if (item == null || item.getDmId() == null || item.getStartTime() == null) {
                continue;
            }
            String status = normalizeSessionStatus(item.getStatus());
            if (STATUS_CANCELLED.equals(status)) {
                continue;
            }
            Date start = item.getStartTime();
            if ((rangeStart != null && start.before(rangeStart)) || (rangeEnd != null && start.after(rangeEnd))) {
                continue;
            }
            Integer dmId = item.getDmId();
            loadMap.put(dmId, loadMap.getOrDefault(dmId, 0) + 1);
        }
        return loadMap;
    }

    private Dm pickBestDmForAutoGeneration(List<Dm> recommendedDms, Map<Integer, Integer> weeklyLoadMap,
            List<Session> allSessions, Date startTime, Date endTime) {
        if (recommendedDms == null || recommendedDms.isEmpty()) {
            return null;
        }
        Dm best = null;
        int bestLoad = Integer.MAX_VALUE;
        for (Dm candidate : recommendedDms) {
            if (candidate == null || candidate.getId() == null) {
                continue;
            }
            if (!"AVAILABLE".equalsIgnoreCase(String.valueOf(candidate.getStatus()))) {
                continue;
            }
            int currentLoad = weeklyLoadMap.getOrDefault(candidate.getId(), 0);
            int weeklyMax = candidate.getWeeklyMaxSessions() == null ? 10 : candidate.getWeeklyMaxSessions();
            if (weeklyMax < 1) {
                weeklyMax = 1;
            }
            if (currentLoad >= weeklyMax) {
                continue;
            }
            if (hasSessionTimeConflict(candidate.getId(), null, startTime, endTime, allSessions)) {
                continue;
            }
            if (hasDmScheduleTimeSlotConflict(candidate.getId(), null, startTime)) {
                continue;
            }
            if (best == null || currentLoad < bestLoad) {
                best = candidate;
                bestLoad = currentLoad;
            }
        }
        return best;
    }

    private void validateDmScheduleSyncConstraints(Session session) {
        if (session == null) {
            return;
        }
        String status = normalizeSessionStatus(session.getStatus());
        if (STATUS_CANCELLED.equals(status)) {
            return;
        }
        if (session.getDmId() == null || session.getStartTime() == null || session.getEndTime() == null) {
            return;
        }
        if (hasSessionTimeConflict(session.getDmId(), session.getId(), session.getStartTime(), session.getEndTime(),
                sessionService.getSessionsByDmId(session.getDmId()))) {
            throw new IllegalStateException("DM在该时段已存在场次冲突，请调整场次时间或更换DM");
        }
        if (hasDmScheduleTimeSlotConflict(session.getDmId(), session.getId(), session.getStartTime())) {
            throw new IllegalStateException("DM在该时段已有排班冲突，请调整场次时间或更换DM");
        }
    }

    private void syncDmScheduleWithSession(Session session, boolean deleteOnly) {
        if (session == null || session.getId() == null) {
            return;
        }
        String status = normalizeSessionStatus(session.getStatus());
        if (deleteOnly || STATUS_CANCELLED.equals(status)) {
            dmScheduleMapper.releaseBySessionId(session.getId());
            return;
        }
        upsertDmScheduleForSession(session);
    }

    private void upsertDmScheduleForSession(Session session) {
        if (session == null || session.getId() == null) {
            return;
        }
        if (session.getDmId() == null || session.getStartTime() == null) {
            throw new IllegalStateException("场次缺少DM或开始时间，无法同步DM排班");
        }
        if (hasSessionTimeConflict(session.getDmId(), session.getId(), session.getStartTime(), session.getEndTime(),
                sessionService.getSessionsByDmId(session.getDmId()))) {
            throw new IllegalStateException("DM在该时段已存在场次冲突，请调整场次时间或更换DM");
        }
        if (hasDmScheduleTimeSlotConflict(session.getDmId(), session.getId(), session.getStartTime())) {
            throw new IllegalStateException("DM在该时段已有排班冲突，请调整场次时间或更换DM");
        }

        DmSchedule existing = dmScheduleMapper.selectBySessionId(session.getId());
        String scheduleDate = toLocalDateTime(session.getStartTime()).toLocalDate().toString();
        String timeSlot = resolveTimeSlot(session.getStartTime());
        if (existing == null) {
            // 优先占用已生成的可用排班槽位，保持“场次-排班”强关联
            List<DmSchedule> sameDateSchedules = dmScheduleMapper.selectByDmIdAndDate(session.getDmId(), scheduleDate);
            if (sameDateSchedules != null && !sameDateSchedules.isEmpty()) {
                for (DmSchedule item : sameDateSchedules) {
                    if (item == null) {
                        continue;
                    }
                    if (!timeSlot.equals(item.getTimeSlot())) {
                        continue;
                    }
                    if (item.getSessionId() != null) {
                        continue;
                    }
                    if (!SCHEDULE_STATUS_AVAILABLE.equalsIgnoreCase(String.valueOf(item.getStatus()))) {
                        continue;
                    }
                    item.setSessionId(session.getId());
                    item.setStatus(SCHEDULE_STATUS_ASSIGNED);
                    dmScheduleMapper.update(item);
                    return;
                }
            }

            DmSchedule schedule = new DmSchedule();
            schedule.setDmId(session.getDmId());
            schedule.setScheduleDate(extractScheduleDate(session.getStartTime()));
            schedule.setTimeSlot(timeSlot);
            schedule.setSessionId(session.getId());
            schedule.setStatus(SCHEDULE_STATUS_ASSIGNED);
            dmScheduleMapper.insert(schedule);
            return;
        }

        existing.setDmId(session.getDmId());
        existing.setScheduleDate(extractScheduleDate(session.getStartTime()));
        existing.setTimeSlot(timeSlot);
        existing.setSessionId(session.getId());
        existing.setStatus(SCHEDULE_STATUS_ASSIGNED);
        dmScheduleMapper.update(existing);
    }

    private boolean hasSessionTimeConflict(Integer dmId, Integer currentSessionId, Date startTime, Date endTime,
            List<Session> dmSessions) {
        if (dmId == null || startTime == null || endTime == null || dmSessions == null || dmSessions.isEmpty()) {
            return false;
        }
        for (Session item : dmSessions) {
            if (item == null || item.getId() == null) {
                continue;
            }
            if (!dmId.equals(item.getDmId())) {
                continue;
            }
            if (currentSessionId != null && currentSessionId.equals(item.getId())) {
                continue;
            }
            if (STATUS_CANCELLED.equals(normalizeSessionStatus(item.getStatus()))) {
                continue;
            }
            Date start = item.getStartTime();
            Date end = item.getEndTime();
            if (start == null || end == null) {
                continue;
            }
            if (start.before(endTime) && end.after(startTime)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDmScheduleTimeSlotConflict(Integer dmId, Integer currentSessionId, Date startTime) {
        if (dmId == null || startTime == null) {
            return false;
        }
        LocalDateTime localDateTime = toLocalDateTime(startTime);
        if (localDateTime == null) {
            return false;
        }
        String date = localDateTime.toLocalDate().toString();
        String slot = resolveTimeSlot(startTime);
        List<DmSchedule> schedules = dmScheduleMapper.selectByDmIdAndDate(dmId, date);
        if (schedules == null || schedules.isEmpty()) {
            return false;
        }
        for (DmSchedule item : schedules) {
            if (item == null) {
                continue;
            }
            if (!slot.equals(item.getTimeSlot())) {
                continue;
            }
            if (currentSessionId != null && item.getSessionId() != null && currentSessionId.equals(item.getSessionId())) {
                continue;
            }
            // 仅“空闲且未绑定场次”的排班槽位可复用，不算冲突
            if (item.getSessionId() == null
                    && SCHEDULE_STATUS_AVAILABLE.equalsIgnoreCase(String.valueOf(item.getStatus()))) {
                continue;
            }
            return true;
        }
        return false;
    }

    private Date extractScheduleDate(Date dateTime) {
        LocalDate localDate = dateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String resolveTimeSlot(Date startTime) {
        LocalDateTime localDateTime = toLocalDateTime(startTime);
        if (localDateTime == null) {
            return "晚上";
        }
        int hour = localDateTime.getHour();
        if (hour < 12) {
            return "上午";
        }
        if (hour < 18) {
            return "下午";
        }
        return "晚上";
    }

    private int normalizeScriptDuration(Integer duration) {
        if (duration == null || duration < 30) {
            return 120;
        }
        if (duration > 600) {
            return 600;
        }
        return duration;
    }

    private String validateSession(Session session, boolean requireId) {
        if (session == null) {
            return "请求参数不能为空";
        }
        if (requireId && session.getId() == null) {
            return "场次ID不能为空";
        }
        if (session.getScriptId() == null || session.getScriptId() < 1) {
            return "剧本ID不能为空";
        }
        if (session.getDmId() == null || session.getDmId() < 1) {
            return "DM ID不能为空";
        }
        if (session.getStartTime() == null) {
            return "开始时间不能为空";
        }
        if (session.getEndTime() == null) {
            return "结束时间不能为空";
        }
        Date start = session.getStartTime();
        Date end = session.getEndTime();
        if (!end.after(start)) {
            return "结束时间必须晚于开始时间";
        }
        String businessTimeError = validateBusinessTime(start, end);
        if (businessTimeError != null) {
            return businessTimeError;
        }
        if (session.getMaxPlayers() == null || session.getMaxPlayers() < 1) {
            return "最大玩家数必须大于0";
        }
        if (session.getCurrentPlayers() != null) {
            if (session.getCurrentPlayers() < 0) {
                return "当前玩家数不能小于0";
            }
            if (session.getCurrentPlayers() > session.getMaxPlayers()) {
                return "当前玩家数不能超过最大玩家数";
            }
        }
        if (session.getStatus() == null || session.getStatus().trim().isEmpty()) {
            return "场次状态不能为空";
        }
        String startValidationError = validateStartCondition(session);
        if (startValidationError != null) {
            return startValidationError;
        }
        return null;
    }

    private String validateBusinessTime(Date start, Date end) {
        int openMinutes = systemRuntimeConfigService.getBusinessOpenMinutes();
        int closeMinutes = systemRuntimeConfigService.getBusinessCloseMinutes();
        LocalDateTime startTime = toLocalDateTime(start);
        LocalDateTime endTime = toLocalDateTime(end);
        if (startTime == null || endTime == null) {
            return null;
        }
        if (!isWithinBusinessHours(startTime, openMinutes, closeMinutes)) {
            return "开始时间不在营业时间内（" + systemRuntimeConfigService.getBusinessOpenLabel() + " - "
                    + systemRuntimeConfigService.getBusinessCloseLabel() + "）";
        }
        if (!isWithinBusinessHours(endTime, openMinutes, closeMinutes)) {
            return "结束时间不在营业时间内（" + systemRuntimeConfigService.getBusinessOpenLabel() + " - "
                    + systemRuntimeConfigService.getBusinessCloseLabel() + "）";
        }
        return null;
    }

    private boolean isWithinBusinessHours(LocalDateTime dateTime, int openMinutes, int closeMinutes) {
        if (openMinutes == closeMinutes) {
            return true;
        }
        int minuteOfDay = dateTime.getHour() * 60 + dateTime.getMinute();
        if (closeMinutes >= 24 * 60) {
            return minuteOfDay >= openMinutes;
        }
        if (openMinutes < closeMinutes) {
            return minuteOfDay >= openMinutes && minuteOfDay <= closeMinutes;
        }
        return minuteOfDay >= openMinutes || minuteOfDay <= closeMinutes;
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private String validateStartCondition(Session session) {
        if (session == null || session.getStatus() == null) {
            return null;
        }
        String status = normalizeSessionStatus(session.getStatus());
        if (!STATUS_ONGOING.equals(status)) {
            return null;
        }
        Script script = scriptService.getScriptById(session.getScriptId());
        if (script == null) {
            return "关联剧本不存在，无法开局";
        }
        Integer minPlayers = script.getMinPlayers();
        if (minPlayers == null || minPlayers < 1) {
            minPlayers = 1;
        }
        int confirmedPlayers;
        if (session.getId() != null) {
            List<Reservation> reservations = reservationService.getReservationsBySessionId(session.getId());
            confirmedPlayers = countConfirmedPlayers(reservations);
            session.setCurrentPlayers(confirmedPlayers);
            int paidPlayers = countPaidPlayers(session.getId(), reservations);
            if (paidPlayers < minPlayers) {
                return "当前已支付人数不足，至少 " + minPlayers + " 人完成支付才能开局";
            }
            if (paidPlayers < confirmedPlayers) {
                return "存在未支付预约，开局前需完成支付（已支付 " + paidPlayers + "/" + confirmedPlayers + " 人）";
            }
        } else {
            Integer currentPlayers = session.getCurrentPlayers();
            confirmedPlayers = currentPlayers == null ? 0 : currentPlayers;
        }
        if (confirmedPlayers < minPlayers) {
            return "当前预约人数不足，至少 " + minPlayers + " 人才能开局";
        }
        return null;
    }

    private String validateStatusOnCreate(Session session) {
        String status = normalizeSessionStatus(session == null ? null : session.getStatus());
        if (!isAllowedStatus(status)) {
            return "场次状态非法";
        }
        if (!STATUS_PENDING.equals(status)) {
            return "新建场次状态只能为等待中";
        }
        session.setStatus(status);
        return null;
    }

    private String validateStatusOnUpdate(Session existing, Session incoming) {
        String oldStatus = normalizeSessionStatus(existing == null ? null : existing.getStatus());
        String newStatus = normalizeSessionStatus(incoming == null ? null : incoming.getStatus());
        if (!isAllowedStatus(newStatus)) {
            return "场次状态非法";
        }
        incoming.setStatus(newStatus);

        if (STATUS_COMPLETED.equals(oldStatus) || STATUS_CANCELLED.equals(oldStatus)) {
            return "已完成或已取消场次不允许编辑";
        }

        if (!oldStatus.equals(newStatus)) {
            if (STATUS_PENDING.equals(oldStatus) && STATUS_CANCELLED.equals(newStatus)) {
                return null;
            }
            return "状态流转受限，请使用开局/完场操作";
        }
        return null;
    }

    private boolean isAllowedStatus(String status) {
        return status != null && ALLOWED_SESSION_STATUSES.contains(status);
    }

    private String normalizeSessionStatus(String status) {
        if (status == null) {
            return "";
        }
        String normalized = status.trim().toUpperCase();
        if (STATUS_IN_PROGRESS.equals(normalized)) {
            return STATUS_ONGOING;
        }
        return normalized;
    }

    private int countConfirmedPlayers(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Reservation reservation : reservations) {
            if (reservation == null) {
                continue;
            }
            String status = reservation.getStatus() == null ? "" : reservation.getStatus().trim().toUpperCase();
            if (!"CONFIRMED".equals(status)) {
                continue;
            }
            Integer players = reservation.getPlayersCount();
            total += (players == null || players < 1) ? 1 : players;
        }
        return total;
    }

    private int countPaidPlayers(Integer sessionId, List<Reservation> reservations) {
        if (sessionId == null || reservations == null || reservations.isEmpty()) {
            return 0;
        }
        List<Order> orders = orderService.getBySessionId(sessionId);
        Set<Integer> paidUserIds = new HashSet<Integer>();
        if (orders != null) {
            for (Order order : orders) {
                if (order == null || order.getUserId() == null) {
                    continue;
                }
                String status = order.getStatus() == null ? "" : order.getStatus().trim().toUpperCase();
                if ("PAID".equals(status) || "COMPLETED".equals(status)) {
                    paidUserIds.add(order.getUserId());
                }
            }
        }
        int total = 0;
        for (Reservation reservation : reservations) {
            if (reservation == null || reservation.getUserId() == null) {
                continue;
            }
            String status = reservation.getStatus() == null ? "" : reservation.getStatus().trim().toUpperCase();
            if (!"CONFIRMED".equals(status) || !paidUserIds.contains(reservation.getUserId())) {
                continue;
            }
            Integer players = reservation.getPlayersCount();
            total += (players == null || players < 1) ? 1 : players;
        }
        return total;
    }

}
