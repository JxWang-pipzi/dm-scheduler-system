package com.example.dm.vo;

import lombok.Data;
import java.util.List;

/**
 * 通用分页结果封装
 */
@Data
public class PageResult<T> {
    /** 总记录数 */
    private Long total;
    /** 当前页数据列表 */
    private List<T> list;
    /** 当前页码 */
    private Integer pageNum;
    /** 每页条数 */
    private Integer pageSize;
    /** 总页数 */
    private Integer totalPages;

    public PageResult() {
    }

    public PageResult(Long total, List<T> list, Integer pageNum, Integer pageSize) {
        this.total = total;
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 快速创建分页结果
     */
    public static <T> PageResult<T> of(Long total, List<T> list, Integer pageNum, Integer pageSize) {
        return new PageResult<>(total, list, pageNum, pageSize);
    }
}
