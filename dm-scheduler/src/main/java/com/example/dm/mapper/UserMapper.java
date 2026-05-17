package com.example.dm.mapper;

import com.example.dm.entity.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserMapper {
    User selectById(Integer id);

    User selectByUsername(String username);

    User selectByEmail(String email);

    List<User> selectAll();

    /** 分页查询（含关键词搜索、角色筛选） */
    List<User> selectPage(@Param("keyword") String keyword, @Param("role") String role,
            @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    /** 统计满足条件的记录数 */
    Long countByCondition(@Param("keyword") String keyword, @Param("role") String role);

    int insert(User user);

    int update(User user);

    int delete(Integer id);
}
