package com.whpu.web;

import com.whpu.annotation.Autowired;
import com.whpu.annotation.Controller;
import com.whpu.annotation.RequestMapping;
import com.whpu.service.UserService;
//key="/user/insert"

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired(value="userService")
    private UserService userService;

    @RequestMapping("/insert")
    public void insert() {
        userService.insert();
    }
    
    @RequestMapping("/add")
    public void add(){
    	userService.add();
    	System.out.println("controller running");
    }

}
