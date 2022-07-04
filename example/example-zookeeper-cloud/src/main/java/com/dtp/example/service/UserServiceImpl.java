package com.dtp.example.service;

import org.apache.dubbo.config.annotation.DubboService;

/**
 * UserServiceImpl related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@DubboService
public class UserServiceImpl implements UserService {

    @Override
    public String getUserName(long id) {
        return "yanhom";
    }
}
