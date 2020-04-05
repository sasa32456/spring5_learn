package com.n33.mvcframework.context;

import com.n33.mvcframework.beans.NBeanWrapper;
import com.n33.mvcframework.beans.config.NBeanDefinition;
import com.n33.mvcframework.beans.support.NBeanDefinitionReader;
import com.n33.mvcframework.beans.support.NDefaultListableBeanFactory;
import com.n33.mvcframework.core.NBeanFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主要实现refresh 和 getBean ，完成ioc DI aop 的衔接
 */
public class NApplicationContext extends NDefaultListableBeanFactory implements NBeanFactory {

    private String[] configLocations;
    private NBeanDefinitionReader reader;

    /**
     * 单例的Ioc容器缓存
     */
    private Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>();
    /**
     * 通用的Ioc容器
     */
    private Map<String, NBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    public NApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void refresh() throws Exception {
        //定位配置文件
        reader = new NBeanDefinitionReader(this.configLocations);
        //加载配置文件，扫描相关类，封装成BeanDefinition
        List<NBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
        //注册，把配置信息放入容器内
        doRegisterBeanDefinition(beanDefinitions);
        //把不是延迟加载的初始化
        doAutowrited();
    }

    /**
     * 只处理非延迟加载的
     */
    private void doAutowrited() {
        for (Map.Entry<String, NBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                String beanName = beanDefinitionEntry.getKey();
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doRegisterBeanDefinition(List<NBeanDefinition> beanDefinitions) throws Exception {
        for (NBeanDefinition beanDefinition : beanDefinitions) {
            if (super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("the \"" + beanDefinition.getFactoryBeanName() + " \" is exists!!");
            }
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }


    @Override
    public Object getBean(Class<?> beanClass) throws Exception {
        return getBean(beanClass.getName());
    }

    /**
     * 依赖注入，读取beanDefinition中的信息
     * 通过反射创建实例并返回
     * Spring做法，封装BeanWrapper
     * 装饰器模式：
     * 保留原OOP关系
     * 扩展（aop等）
     *
     * @param beanName
     * @return
     * @throws Exception
     */
    @Override
    public Object getBean(String beanName) throws Exception {
        return null;
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }
}
