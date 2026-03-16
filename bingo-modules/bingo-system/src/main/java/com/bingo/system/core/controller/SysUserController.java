package com.bingo.system.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SysUserController {


    @RequestMapping("/sysUserDao")
    public String getSysUserDao() {

//        UserCond cond = UserCond.builder().build();
//        List<SysUser> listUser = sysUserDao.list(new UserCond());
//        log.info("listUser:{}", listUser);

        return "sysUserDao";
    }
}
