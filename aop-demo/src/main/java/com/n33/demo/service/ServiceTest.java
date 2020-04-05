package com.n33.demo.service;

import org.springframework.stereotype.Service;

@Service
public class ServiceTest {
    public void doIt() {
        System.out.println("=========== do it  ==================");
    }

    public void doItIdInt(int id) throws RuntimeException{
        if (id == 1) {
            throw new RuntimeException("============= 报错了 ================");
        }
        System.out.println("=========== do it id : " + id + " ==================");
    }

    public Integer doItIdInteger(Integer id){
        System.out.println("=========== do it id : " + id + " ==================");
        return id;
    }
}
