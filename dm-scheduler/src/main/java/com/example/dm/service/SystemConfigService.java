package com.example.dm.service;

import com.example.dm.entity.SystemConfig;

import java.util.List;

public interface SystemConfigService {
    boolean createConfig(SystemConfig systemConfig);
    boolean updateConfig(SystemConfig systemConfig);
    boolean deleteConfig(Integer id);
    SystemConfig getConfigById(Integer id);
    SystemConfig getConfigByKey(String configKey);
    List<SystemConfig> getAllConfigs();
}
