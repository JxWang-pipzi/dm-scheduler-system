package com.example.dm.service;

import com.example.dm.entity.User;
import com.example.dm.vo.PageResult;
import java.util.List;

public interface UserService {
    User getUserById(Integer id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    List<User> getAllUsers();

    /** 分页查询用户 */
    PageResult<User> getUserPage(String keyword, String role, Integer pageNum, Integer pageSize);

    User addUser(User user);

    User updateUser(User user);

    boolean deleteUser(Integer id);

    User login(String username, String password);
}
