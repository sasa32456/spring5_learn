package com.n33.mvcframework.beans.support;

import com.n33.mvcframework.beans.config.NBeanDefinition;
import com.n33.mvcframework.context.support.NAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NDefaultListableBeanFactory extends NAbstractApplicationContext {

    /**
     * 存储注册信息的BeanDefinition
     */
    protected final Map<String, NBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

}
