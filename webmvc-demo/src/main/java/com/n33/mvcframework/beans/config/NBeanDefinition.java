package com.n33.mvcframework.beans.config;

import lombok.Data;

/**
 * 用来存储配置文件信息
 */
@Data
public class NBeanDefinition {
    /**
     * 原生Bean全类名
     */
    private String beanClassName;
    /**
     * 是否延迟加载
     */
    private boolean lazyInit = false;
    /**
     * 保存beanName，在Ioc中存储key
     */
    private String factoryBeanName;

    public boolean isLazyInit(){
        return lazyInit;
    }

}
