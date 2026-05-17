package com.example.dm.controller;

import com.example.dm.mapper.OperationLogMapper;
import com.example.dm.entity.OperationLog;
import com.example.dm.util.Result;
import com.example.dm.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志Controller - 提供操作日志的分页查询
 */
@RestController
@RequestMapping("/api/operation-log")
public class OperationLogController {

    @Autowired
    private OperationLogMapper operationLogMapper;

    /** 分页查询操作日志 */
    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String action) {
        try {
            int offset = (pageNum - 1) * pageSize;
            List<OperationLog> list = operationLogMapper.selectPage(action, keyword, null, null, offset, pageSize);
            Long total = operationLogMapper.countByCondition(action, keyword, null, null);
            PageResult<OperationLog> page = PageResult.of(total, list, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(5000, "获取操作日志失败: " + e.getMessage());
        }
    }
}
