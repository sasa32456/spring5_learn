package com.n33.demo.service.impl;

import com.n33.demo.service.DemoService;
import com.n33.mvcframework.annotation.NService;

@NService
public class DemoServiceImpl implements DemoService {
    @Override
    public String get(String name) {
        return "My name is " + name;
    }
}
