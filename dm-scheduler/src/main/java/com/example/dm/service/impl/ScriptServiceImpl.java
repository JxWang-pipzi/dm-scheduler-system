package com.example.dm.service.impl;

import com.example.dm.entity.Script;
import com.example.dm.mapper.ScriptMapper;
import com.example.dm.service.ScriptService;
import com.example.dm.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScriptServiceImpl implements ScriptService {

    @Autowired
    private ScriptMapper scriptMapper;

    @Override
    public Script getScriptById(Integer id) {
        return scriptMapper.selectById(id);
    }

    @Override
    public List<Script> getAllScripts() {
        return scriptMapper.selectAll();
    }

    @Override
    public List<Script> getScriptsByType(String type) {
        return scriptMapper.selectByType(type);
    }

    @Override
    public PageResult<Script> getScriptPage(String keyword, String type, String difficulty, Integer pageNum,
            Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Script> list = scriptMapper.selectPage(keyword, type, difficulty, offset, pageSize);
        Long total = scriptMapper.countByCondition(keyword, type, difficulty);
        return PageResult.of(total, list, pageNum, pageSize);
    }

    @Override
    public Script addScript(Script script) {
        int affected = scriptMapper.insert(script);
        if (affected <= 0)
            return null;
        return scriptMapper.selectById(script.getId());
    }

    @Override
    public Script updateScript(Script script) {
        int affected = scriptMapper.update(script);
        if (affected <= 0)
            return null;
        return scriptMapper.selectById(script.getId());
    }

    @Override
    public boolean deleteScript(Integer id) {
        return scriptMapper.delete(id) > 0;
    }

    @Override
    public List<Script> getHotScripts(Integer limit) {
        return scriptMapper.selectHotScripts(limit);
    }
}