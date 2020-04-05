package com.whpu.service;

import com.whpu.annotation.Autowired;
import com.whpu.annotation.Service;
import com.whpu.dao.UserDao;

@Service
public class UserService {

    @Autowired("userDao")
    private UserDao userDao;

    public void insert() {
        userDao.insert();
    }
    public void add() {
    	userDao.add();
    	System.out.println("service running");
    }
    
}
