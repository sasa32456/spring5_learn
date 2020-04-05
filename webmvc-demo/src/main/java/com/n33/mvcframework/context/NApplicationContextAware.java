package com.n33.mvcframework.context;

/**
 * 钩子回调，在类中set注入NApplicationContext（未实现）
 */
public interface NApplicationContextAware {
    void setApplicationContext(NApplicationContext applicationContext);
}
