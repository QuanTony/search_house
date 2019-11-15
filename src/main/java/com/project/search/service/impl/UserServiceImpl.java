package com.project.search.service.impl;

import com.project.search.common.utils.MD5Util;
import com.project.search.dao.mappers.UserMapper;
import com.project.search.dao.model.User;
import com.project.search.dao.model.UserExample;
import com.project.search.entity.param.LoginParam;
import com.project.search.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Override
    public boolean checkUserExist(LoginParam loginParam) {
        boolean isExist = false;

        UserExample userExample = new UserExample();
        userExample.createCriteria().andNameEqualTo(loginParam.getLoginName());
        Long count = userMapper.countByExample(userExample);
        if (count > 0) {
            isExist = true;
        }
        return isExist;
    }

    @Override
    public boolean checkPassword(LoginParam loginParam) {
        //获取数据库用户信息
        User user = null;
        UserExample userExample = new UserExample();
        userExample.createCriteria().andNameEqualTo(loginParam.getLoginName());
        List<User> userList = userMapper.selectByExample(userExample);
        if (userList.size() > 0){
            user = userList.get(0);
        }
        //把传入的密码加盐后和数据库的对比
        return MD5Util.checkPwd(loginParam.getPassword(),user.getPassword(),String.valueOf(user.getId()));
    }

    @Override
    public User getUser(LoginParam loginParam) {
        //获取数据库用户信息
        User user = null;
        UserExample userExample = new UserExample();
        userExample.createCriteria().andNameEqualTo(loginParam.getLoginName());
        List<User> userList = userMapper.selectByExample(userExample);
        if (userList.size() > 0){
            user = userList.get(0);
        }
        return user;
    }

    @Override
    public User getById(Long id) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdEqualTo(id);
        User user = userMapper.selectByExample(userExample).get(0);
        return user;
    }
}
