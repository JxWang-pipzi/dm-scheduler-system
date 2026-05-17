package com.example.dm.mapper;

import com.example.dm.entity.Dm;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface DmMapper {
    Dm selectById(Integer id);

    Dm selectByUserId(Integer userId);

    List<Dm> selectAll();

    List<Dm> selectByLevel(Integer level);

    /** 分页查询（含关键词搜索、状态筛选） */
    List<Dm> selectPage(@Param("keyword") String keyword, @Param("status") String status,
            @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    /** 统计满足条件的记录数 */
    Long countByCondition(@Param("keyword") String keyword, @Param("status") String status);

    /** 查询可用DM（按等级过滤） */
    List<Dm> selectAvailableByLevel(@Param("level") Integer level);

    int insert(Dm dm);

    int update(Dm dm);

    int delete(Integer id);
}