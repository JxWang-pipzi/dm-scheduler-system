package com.example.dm.controller;

import com.example.dm.entity.SystemConfig;
import com.example.dm.service.SystemConfigService;
import com.example.dm.service.SystemRuntimeConfigService;
import com.example.dm.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/config")
public class SystemConfigController {
    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SystemRuntimeConfigService systemRuntimeConfigService;

    @PostMapping
    public Result createConfig(@RequestBody SystemConfig systemConfig) {
        String error = validateConfig(systemConfig, false);
        if (error != null) {
            return Result.error(4000, error);
        }
        boolean success = systemConfigService.createConfig(systemConfig);
        return success ? Result.success() : Result.error("创建配置失败，配置键可能已存在");
    }

    @PutMapping
    public Result updateConfig(@RequestBody SystemConfig systemConfig) {
        String error = validateConfig(systemConfig, true);
        if (error != null) {
            return Result.error(4000, error);
        }
        boolean success = systemConfigService.updateConfig(systemConfig);
        return success ? Result.success() : Result.error("更新配置失败");
    }

    @DeleteMapping("/{id}")
    public Result deleteConfig(@PathVariable Integer id) {
        boolean success = systemConfigService.deleteConfig(id);
        return success ? Result.success() : Result.error("删除配置失败");
    }

    @GetMapping("/{id}")
    public Result getConfigById(@PathVariable Integer id) {
        SystemConfig systemConfig = systemConfigService.getConfigById(id);
        return systemConfig != null ? Result.success(systemConfig) : Result.error("配置不存在");
    }

    @GetMapping("/key/{configKey}")
    public Result getConfigByKey(@PathVariable String configKey) {
        SystemConfig systemConfig = systemConfigService.getConfigByKey(configKey);
        return systemConfig != null ? Result.success(systemConfig) : Result.error("配置不存在");
    }

    @GetMapping
    public Result getAllConfigs() {
        List<SystemConfig> systemConfigs = systemConfigService.getAllConfigs();
        return Result.success(systemConfigs);
    }

    @GetMapping("/effective")
    public Result getEffectiveConfigs() {
        Map<String, String> effectiveConfigs = systemRuntimeConfigService.getEffectiveConfigs();
        return Result.success(effectiveConfigs);
    }

    @GetMapping("/runtime")
    public Result getRuntimeConfigs() {
        Map<String, Object> runtimeConfigs = new LinkedHashMap<String, Object>();
        runtimeConfigs.put(SystemRuntimeConfigService.KEY_SYSTEM_NAME, systemRuntimeConfigService.getSystemName());
        runtimeConfigs.put(SystemRuntimeConfigService.KEY_OPEN_TIME, systemRuntimeConfigService.getBusinessOpenLabel());
        runtimeConfigs.put(SystemRuntimeConfigService.KEY_CLOSE_TIME, systemRuntimeConfigService.getBusinessCloseLabel());
        runtimeConfigs.put(SystemRuntimeConfigService.KEY_DEFAULT_SESSION_MAX_PLAYERS,
                systemRuntimeConfigService.getDefaultSessionMaxPlayers());
        runtimeConfigs.put(SystemRuntimeConfigService.KEY_RESERVATION_LEAD_HOURS,
                systemRuntimeConfigService.getReservationLeadHours());
        return Result.success(runtimeConfigs);
    }

    private String validateConfig(SystemConfig systemConfig, boolean requireId) {
        if (systemConfig == null) {
            return "配置不能为空";
        }
        if (requireId && systemConfig.getId() == null) {
            return "配置ID不能为空";
        }
        if (systemConfig.getConfigKey() == null || systemConfig.getConfigKey().trim().isEmpty()) {
            return "配置键不能为空";
        }
        if (systemConfig.getConfigValue() == null || systemConfig.getConfigValue().trim().isEmpty()) {
            return "配置值不能为空";
        }
        return null;
    }
}
