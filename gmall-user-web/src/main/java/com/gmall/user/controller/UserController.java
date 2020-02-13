package com.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.UmsMember;
import com.gmall.bean.UmsMemberReceiveAddress;
import com.gmall.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@RestController
public class UserController {
    @Reference
    UserService userService;

    @GetMapping("/getAllUser")
    public List<UmsMember> getAllUser(){
        return userService.getAllUser();
    }

    @GetMapping("/getReceiveAddressByMemberId")
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId){

        return userService.getReceiveAddressByMemberId(memberId);
    }
}
