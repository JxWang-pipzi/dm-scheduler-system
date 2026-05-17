package com.example.dm.mapper;

import com.example.dm.entity.Script;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ScriptMapper {
        Script selectById(Integer id);

        List<Script> selectAll();

        List<Script> selectByType(String type);

        /** 分页查询（含关键词搜索、类型/难度筛选） */
        List<Script> selectPage(@Param("keyword") String keyword, @Param("type") String type,
                        @Param("difficulty") String difficulty,
                        @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

        /** 统计满足条件的记录数 */
        Long countByCondition(@Param("keyword") String keyword, @Param("type") String type,
                        @Param("difficulty") String difficulty);

        int insert(Script script);

        int update(Script script);

        int delete(Integer id);

        /** 获取热门推荐剧本 */
        List<Script> selectHotScripts(@Param("limit") Integer limit);
}