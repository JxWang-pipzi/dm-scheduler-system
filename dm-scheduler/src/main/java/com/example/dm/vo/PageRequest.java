package com.example.dm.vo;

import lombok.Data;

/**
 * 通用分页请求参数
 */
@Data
public class PageRequest {
    /** 页码，默认第1页 */
    private Integer pageNum = 1;
    /** 每页条数，默认10条 */
    private Integer pageSize = 10;
    /** 搜索关键词 */
    private String keyword;

    /**
     * 获取SQL的OFFSET偏移量
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
