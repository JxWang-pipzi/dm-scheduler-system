package com.example.dm.mapper;

import com.example.dm.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SystemConfigMapper {
    int insert(SystemConfig systemConfig);
    int update(SystemConfig systemConfig);
    int delete(Integer id);
    SystemConfig selectById(Integer id);
    SystemConfig selectByKey(String configKey);
    List<SystemConfig> selectAll();
}
