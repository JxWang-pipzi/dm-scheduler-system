package com.example.dm.service;

import com.example.dm.entity.SystemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SystemRuntimeConfigService {
    public static final String KEY_SYSTEM_NAME = "system_name";
    public static final String KEY_OPEN_TIME = "open_time";
    public static final String KEY_CLOSE_TIME = "close_time";
    public static final String KEY_DEFAULT_SESSION_MAX_PLAYERS = "default_session_max_players";
    public static final String KEY_RESERVATION_LEAD_HOURS = "reservation_lead_hours";

    private static final String DEFAULT_SYSTEM_NAME = "剧本杀调度管理系统";
    private static final String DEFAULT_OPEN_TIME = "10:00";
    private static final String DEFAULT_CLOSE_TIME = "24:00";
    private static final int DEFAULT_SESSION_MAX_PLAYERS = 6;
    private static final int DEFAULT_RESERVATION_LEAD_HOURS = 0;

    @Autowired
    private SystemConfigService systemConfigService;

    public Map<String, String> getEffectiveConfigs() {
        Map<String, String> effective = new LinkedHashMap<String, String>();
        effective.put(KEY_SYSTEM_NAME, DEFAULT_SYSTEM_NAME);
        effective.put(KEY_OPEN_TIME, DEFAULT_OPEN_TIME);
        effective.put(KEY_CLOSE_TIME, DEFAULT_CLOSE_TIME);
        effective.put(KEY_DEFAULT_SESSION_MAX_PLAYERS, String.valueOf(DEFAULT_SESSION_MAX_PLAYERS));
        effective.put(KEY_RESERVATION_LEAD_HOURS, String.valueOf(DEFAULT_RESERVATION_LEAD_HOURS));

        List<SystemConfig> customConfigs = systemConfigService.getAllConfigs();
        if (customConfigs == null) {
            return effective;
        }
        for (SystemConfig item : customConfigs) {
            if (item == null) {
                continue;
            }
            String key = trimToEmpty(item.getConfigKey());
            if (key.isEmpty()) {
                continue;
            }
            String value = trimToEmpty(item.getConfigValue());
            if (value.isEmpty()) {
                continue;
            }
            effective.put(key, value);
        }
        return effective;
    }

    public String getSystemName() {
        return getString(KEY_SYSTEM_NAME, DEFAULT_SYSTEM_NAME);
    }

    public int getDefaultSessionMaxPlayers() {
        return getInt(KEY_DEFAULT_SESSION_MAX_PLAYERS, DEFAULT_SESSION_MAX_PLAYERS, 1, 30);
    }

    public int getReservationLeadHours() {
        return getInt(KEY_RESERVATION_LEAD_HOURS, DEFAULT_RESERVATION_LEAD_HOURS, 0, 168);
    }

    public int getBusinessOpenMinutes() {
        return parseTimeMinutes(getString(KEY_OPEN_TIME, DEFAULT_OPEN_TIME), 10 * 60);
    }

    public int getBusinessCloseMinutes() {
        return parseTimeMinutes(getString(KEY_CLOSE_TIME, DEFAULT_CLOSE_TIME), 24 * 60);
    }

    public String getBusinessOpenLabel() {
        return toTimeLabel(getBusinessOpenMinutes());
    }

    public String getBusinessCloseLabel() {
        return toTimeLabel(getBusinessCloseMinutes());
    }

    public String getString(String key, String defaultValue) {
        String normalizedKey = trimToEmpty(key);
        if (normalizedKey.isEmpty()) {
            return defaultValue;
        }
        String value = getEffectiveConfigs().get(normalizedKey);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    public int getInt(String key, int defaultValue, int minValue, int maxValue) {
        String text = getString(key, String.valueOf(defaultValue));
        try {
            int value = Integer.parseInt(text.trim());
            if (value < minValue || value > maxValue) {
                return defaultValue;
            }
            return value;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public String toTimeLabel(int minutes) {
        if (minutes <= 0) {
            return "00:00";
        }
        if (minutes >= 24 * 60) {
            return "24:00";
        }
        int hour = minutes / 60;
        int minute = minutes % 60;
        return String.format(Locale.ROOT, "%02d:%02d", hour, minute);
    }

    public int parseTimeMinutes(String text, int fallback) {
        String value = trimToEmpty(text);
        if (value.isEmpty()) {
            return fallback;
        }
        if ("24:00".equals(value)) {
            return 24 * 60;
        }
        String[] parts = value.split(":");
        if (parts.length != 2) {
            return fallback;
        }
        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return fallback;
            }
            return hour * 60 + minute;
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
