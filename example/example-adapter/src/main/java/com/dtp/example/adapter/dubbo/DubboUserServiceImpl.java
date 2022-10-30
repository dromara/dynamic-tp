package com.dtp.example.adapter.dubbo;

import org.apache.dubbo.config.annotation.DubboService;

/**
 * DubboUserServiceImpl related
 *
 * @author yanhom
 * @since 1.0.9
 **/
@DubboService
public class DubboUserServiceImpl implements DubboUserService {

    @Override
    public String getUserName(long id) {
        return "yanhom";
    }
}
