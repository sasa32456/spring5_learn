package com.n33.mvcframework.beans;
/**
 * 用于封装创建后对象实例，代理对象
 *
 */
public class NBeanWrapper {
    private Object wrappedInstance;
    private Class<?> wrappedClass;

    public NBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance() {
        return wrappedInstance;
    }

    /**
     * 返回代理后的Class，可能为$Proxy0
     *
     * @return
     */
    public Class<?> getWrappedClass() {
        return wrappedInstance.getClass();
    }
}
