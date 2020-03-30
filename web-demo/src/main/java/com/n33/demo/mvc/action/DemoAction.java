package com.n33.demo.mvc.action;

import com.n33.demo.service.DemoService;
import com.n33.mvcframework.annotation.NAutowired;
import com.n33.mvcframework.annotation.NController;
import com.n33.mvcframework.annotation.NRequestMapping;
import com.n33.mvcframework.annotation.NRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@NController
@NRequestMapping("/demo")
public class DemoAction {
    @NAutowired
    private DemoService demoService;

    @NRequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp, @NRequestParam("name") String name) {
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NRequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp, @NRequestParam("a") Integer a, @NRequestParam("b") Integer b) {
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NRequestMapping("/remove")
    public void remove(HttpServletRequest req, HttpServletResponse resp, @NRequestParam("id") Integer id) {

    }
}
