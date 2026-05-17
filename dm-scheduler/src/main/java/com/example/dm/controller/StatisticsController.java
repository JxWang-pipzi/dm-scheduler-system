package com.example.dm.controller;

import com.example.dm.entity.Dm;
import com.example.dm.entity.Order;
import com.example.dm.entity.Script;
import com.example.dm.entity.Session;
import com.example.dm.service.DmService;
import com.example.dm.service.OrderService;
import com.example.dm.service.ScriptService;
import com.example.dm.service.SessionService;
import com.example.dm.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private DmService dmService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/session")
    public Result getSessionStatistics() {
        List<Session> sessions = safeList(sessionService.getAllSessions());
        Map<String, Object> statistics = buildSessionStatistics(sessions);
        return Result.success(statistics);
    }

    @GetMapping("/dm")
    public Result getDmStatistics() {
        List<Dm> dms = safeList(dmService.getAllDms());
        Map<String, Object> statistics = new HashMap<>();
        int availableDms = 0;
        int busyDms = 0;
        int restDms = 0;

        for (Dm dm : dms) {
            String status = normalizeStatus(dm.getStatus());
            if ("AVAILABLE".equals(status)) {
                availableDms++;
            } else if ("BUSY".equals(status) || "ASSIGNED".equals(status)) {
                busyDms++;
            } else {
                restDms++;
            }
        }

        statistics.put("totalDms", dms.size());
        statistics.put("busyDms", busyDms);
        statistics.put("availableDms", availableDms);
        statistics.put("restDms", restDms);
        return Result.success(statistics);
    }

    @GetMapping("/script")
    public Result getScriptStatistics() {
        List<Script> scripts = safeList(scriptService.getAllScripts());
        List<Session> sessions = safeList(sessionService.getAllSessions());

        Map<Integer, Long> scriptSessionCount = sessions.stream()
                .filter(s -> s.getScriptId() != null)
                .collect(Collectors.groupingBy(Session::getScriptId, Collectors.counting()));

        long popularScripts = scripts.stream()
                .map(Script::getId)
                .filter(id -> id != null && scriptSessionCount.getOrDefault(id, 0L) > 0L)
                .count();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalScripts", scripts.size());
        statistics.put("popularScripts", popularScripts);
        return Result.success(statistics);
    }

    @GetMapping("/income")
    public Result getIncomeStatistics() {
        List<Order> orders = safeList(orderService.getAllOrders());
        YearMonth currentMonth = YearMonth.now();

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal monthlyIncome = BigDecimal.ZERO;

        for (Order order : orders) {
            if (!isCountableIncomeOrder(order)) {
                continue;
            }
            BigDecimal amount = safeAmount(order.getTotalPrice());
            totalIncome = totalIncome.add(amount);

            YearMonth orderMonth = toYearMonth(resolveOrderTime(order));
            if (currentMonth.equals(orderMonth)) {
                monthlyIncome = monthlyIncome.add(amount);
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalIncome", totalIncome.doubleValue());
        statistics.put("monthlyIncome", monthlyIncome.doubleValue());
        return Result.success(statistics);
    }

    @GetMapping("/charts")
    public Result getChartStatistics() {
        List<Session> sessions = safeList(sessionService.getAllSessions());
        List<Dm> dms = safeList(dmService.getAllDms());
        List<Script> scripts = safeList(scriptService.getAllScripts());
        List<Order> orders = safeList(orderService.getAllOrders());

        Map<String, Object> data = new HashMap<>();
        data.put("sessionTrend", buildSessionTrend(sessions, 7));
        data.put("dmWorkload", buildDmWorkload(sessions, dms, 8));
        data.put("scriptHeat", buildScriptHeat(sessions, scripts, 8));
        data.put("incomeTrend", buildIncomeTrend(orders, 6));
        data.put("scriptTypeDistribution", buildScriptTypeDistribution(scripts));

        return Result.success(data);
    }

    private Map<String, Object> buildSessionStatistics(List<Session> sessions) {
        long completed = 0L;
        long pending = 0L;

        for (Session session : sessions) {
            String status = normalizeStatus(session.getStatus());
            if ("COMPLETED".equals(status)) {
                completed++;
            } else if ("PENDING".equals(status) || "ONGOING".equals(status) || "IN_PROGRESS".equals(status)) {
                pending++;
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalSessions", sessions.size());
        statistics.put("pendingSessions", pending);
        statistics.put("completedSessions", completed);
        return statistics;
    }

    private Map<String, Object> buildSessionTrend(List<Session> sessions, int days) {
        LinkedHashMap<LocalDate, Integer> counter = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            counter.put(today.minusDays(i), 0);
        }

        for (Session session : sessions) {
            LocalDate date = toLocalDate(session.getStartTime());
            if (date != null && counter.containsKey(date)) {
                counter.put(date, counter.get(date) + 1);
            }
        }

        List<String> labels = counter.keySet().stream()
                .map(d -> d.format(DateTimeFormatter.ofPattern("M月d日")))
                .collect(Collectors.toList());
        List<Integer> values = new ArrayList<>(counter.values());

        Map<String, Object> trend = new HashMap<>();
        trend.put("labels", labels);
        trend.put("values", values);
        return trend;
    }

    private Map<String, Object> buildDmWorkload(List<Session> sessions, List<Dm> dms, int topN) {
        Map<Integer, Long> workload = sessions.stream()
                .filter(s -> s.getDmId() != null)
                .collect(Collectors.groupingBy(Session::getDmId, Collectors.counting()));

        List<Map<String, Object>> items = new ArrayList<>();
        for (Dm dm : dms) {
            if (dm.getId() == null) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("label", "DM-" + dm.getId());
            item.put("value", workload.getOrDefault(dm.getId(), 0L));
            items.add(item);
        }

        items.sort((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")));
        if (items.size() > topN) {
            items = items.subList(0, topN);
        }

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (Map<String, Object> item : items) {
            labels.add((String) item.get("label"));
            values.add((Long) item.get("value"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("values", values);
        return result;
    }

    private Map<String, Object> buildScriptHeat(List<Session> sessions, List<Script> scripts, int topN) {
        Map<Integer, Long> scriptCounter = sessions.stream()
                .filter(s -> s.getScriptId() != null)
                .collect(Collectors.groupingBy(Session::getScriptId, Collectors.counting()));

        Map<Integer, String> scriptNameMap = scripts.stream()
                .filter(s -> s.getId() != null)
                .collect(Collectors.toMap(Script::getId, s -> safeText(s.getScriptName(), "未命名剧本"), (a, b) -> a));

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : scriptNameMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("label", entry.getValue());
            item.put("value", scriptCounter.getOrDefault(entry.getKey(), 0L));
            items.add(item);
        }

        items.sort((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")));
        if (items.size() > topN) {
            items = items.subList(0, topN);
        }

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (Map<String, Object> item : items) {
            labels.add((String) item.get("label"));
            values.add((Long) item.get("value"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("values", values);
        return result;
    }

    private Map<String, Object> buildIncomeTrend(List<Order> orders, int months) {
        LinkedHashMap<YearMonth, BigDecimal> counter = new LinkedHashMap<>();
        YearMonth current = YearMonth.now();
        for (int i = months - 1; i >= 0; i--) {
            counter.put(current.minusMonths(i), BigDecimal.ZERO);
        }

        for (Order order : orders) {
            if (!isCountableIncomeOrder(order)) {
                continue;
            }
            YearMonth month = toYearMonth(resolveOrderTime(order));
            if (month != null && counter.containsKey(month)) {
                counter.put(month, counter.get(month).add(safeAmount(order.getTotalPrice())));
            }
        }

        List<String> labels = counter.keySet().stream()
                .map(m -> m.getMonthValue() + "月")
                .collect(Collectors.toList());
        List<Double> values = counter.values().stream()
                .map(BigDecimal::doubleValue)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("values", values);
        return result;
    }

    private List<Map<String, Object>> buildScriptTypeDistribution(List<Script> scripts) {
        Map<String, Long> counter = scripts.stream()
                .collect(Collectors.groupingBy(s -> safeText(s.getType(), "未分类"), Collectors.counting()));

        List<Map<String, Object>> distribution = new ArrayList<>();
        for (Map.Entry<String, Long> entry : counter.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            distribution.add(item);
        }

        distribution.sort((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")));
        return distribution;
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }

    private boolean isCountableIncomeOrder(Order order) {
        String status = normalizeStatus(order.getStatus());
        return "PAID".equals(status) || "COMPLETED".equals(status);
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private LocalDate toLocalDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZONE_ID).toLocalDate();
    }

    private LocalDateTime resolveOrderTime(Order order) {
        java.util.Date base = order.getPayTime() != null ? order.getPayTime() : order.getCreateTime();
        if (base == null) {
            return null;
        }
        return base.toInstant().atZone(ZONE_ID).toLocalDateTime();
    }

    private YearMonth toYearMonth(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return YearMonth.of(time.getYear(), time.getMonthValue());
    }

    private String safeText(String text, String fallback) {
        if (text == null || text.trim().isEmpty()) {
            return fallback;
        }
        return text.trim();
    }
}
