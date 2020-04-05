package com.n33.demo;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Component
@Aspect
//@Slf4j
public class AnnotaionAspect {
    @Pointcut("execution(* com.n33.demo.service..*(..))")
    public void aspect(){}

    @Before("aspect()")
    public void before(JoinPoint joinPoint){
        System.out.println("================ this is before ===============");
    }

    @Around("aspect()")
    public void around(JoinPoint joinPoint) throws RuntimeException{
        try {
            System.out.println("======================= before around ===============");
            ((ProceedingJoinPoint) joinPoint).proceed();
            System.out.println("======================= after around ===============");
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }


    @AfterReturning("aspect()")
    public void afterReturn(JoinPoint joinPoint) {
        System.out.println("============== afterReturn ================");
    }

    @AfterThrowing(pointcut = "aspect()" , throwing = "ex")
    public void afterThrow(JoinPoint joinPoint,Exception ex) {
        System.out.println("============== afterThrow : " + ex.getMessage() + " ===================");
    }

    @Before("execution(java.lang.Integer com.n33.demo.service..*(java.lang.Integer))&&args(id)")
    public void beforeInteger(JoinPoint joinPoint, Integer id) {
        System.out.println("============== beforeArgId : " + id +" =================");
    }


}
