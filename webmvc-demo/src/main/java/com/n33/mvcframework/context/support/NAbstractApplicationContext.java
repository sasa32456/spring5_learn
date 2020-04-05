package com.n33.mvcframework.context.support;

/**
 * ioc 容器顶层设计
 */
public abstract class NAbstractApplicationContext {
    /**
     * 受保护，只提供给子类重写
     * @throws Exception
     */
    public void refresh()throws Exception {

    }

}
