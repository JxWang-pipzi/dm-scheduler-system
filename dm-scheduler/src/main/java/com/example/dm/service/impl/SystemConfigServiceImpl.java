package com.example.dm.service.impl;

import com.example.dm.entity.SystemConfig;
import com.example.dm.mapper.SystemConfigMapper;
import com.example.dm.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {
    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Override
    public boolean createConfig(SystemConfig systemConfig) {
        return systemConfigMapper.insert(systemConfig) > 0;
    }

    @Override
    public boolean updateConfig(SystemConfig systemConfig) {
        return systemConfigMapper.update(systemConfig) > 0;
    }

    @Override
    public boolean deleteConfig(Integer id) {
        return systemConfigMapper.delete(id) > 0;
    }

    @Override
    public SystemConfig getConfigById(Integer id) {
        return systemConfigMapper.selectById(id);
    }

    @Override
    public SystemConfig getConfigByKey(String configKey) {
        return systemConfigMapper.selectByKey(configKey);
    }

    @Override
    public List<SystemConfig> getAllConfigs() {
        return systemConfigMapper.selectAll();
    }
}
