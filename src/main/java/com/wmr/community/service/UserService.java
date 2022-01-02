package com.wmr.community.service;

import com.wmr.community.dao.UserMapper;
import com.wmr.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserMapper userMapper;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }
}
