package com.dtp.example.adapter.motan;

/**
 * FooServiceImpl related
 *
 * @author yanhom
 * @since 1.1.0
 */
public class FooServiceImpl implements FooService {

    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
