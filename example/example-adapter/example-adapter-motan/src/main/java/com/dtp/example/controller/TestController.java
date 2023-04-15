package com.dtp.example.controller;

import com.dtp.example.motan.FooService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author fabian4
 */
@Slf4j
@RestController
@SuppressWarnings("all")
public class TestController {

    @Autowired
    private FooService fooService;

    @GetMapping("/dtp-example-adapter/testMotan")
    public String testMotan() throws InterruptedException {
        return fooService.hello("dynamtic-tp");
    }

}
