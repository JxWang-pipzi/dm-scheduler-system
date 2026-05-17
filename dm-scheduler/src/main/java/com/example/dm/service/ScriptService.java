package com.example.dm.service;

import com.example.dm.entity.Script;
import com.example.dm.vo.PageResult;
import java.util.List;

public interface ScriptService {
    Script getScriptById(Integer id);

    List<Script> getAllScripts();

    List<Script> getScriptsByType(String type);

    /** 分页查询剧本 */
    PageResult<Script> getScriptPage(String keyword, String type, String difficulty, Integer pageNum, Integer pageSize);

    Script addScript(Script script);

    Script updateScript(Script script);

    boolean deleteScript(Integer id);

    List<Script> getHotScripts(Integer limit);
}