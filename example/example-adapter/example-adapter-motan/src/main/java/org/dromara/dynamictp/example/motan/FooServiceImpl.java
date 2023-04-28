package org.dromara.dynamictp.example.motan;

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
