package com.project.search.service;


import com.project.search.dao.model.User;
import com.project.search.entity.param.LoginParam;

public interface UserService {

    public boolean checkUserExist(LoginParam loginParam);

    public boolean checkPassword(LoginParam loginParam);

    public User getUser(LoginParam loginParam);

    public User getById(Long id);
}
