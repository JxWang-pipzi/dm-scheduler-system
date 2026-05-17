package com.example.dm.service.impl;

import com.example.dm.entity.User;
import com.example.dm.mapper.UserMapper;
import com.example.dm.service.UserService;
import com.example.dm.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserById(Integer id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    @Override
    public PageResult<User> getUserPage(String keyword, String role, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<User> list = userMapper.selectPage(keyword, role, offset, pageSize);
        Long total = userMapper.countByCondition(keyword, role);
        return PageResult.of(total, list, pageNum, pageSize);
    }

    @Override
    public User addUser(User user) {
        int affected = userMapper.insert(user);
        if (affected <= 0)
            return null;
        return userMapper.selectById(user.getId());
    }

    @Override
    public User updateUser(User user) {
        int affected = userMapper.update(user);
        if (affected <= 0)
            return null;
        return userMapper.selectById(user.getId());
    }

    @Override
    public boolean deleteUser(Integer id) {
        return userMapper.delete(id) > 0;
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
}
