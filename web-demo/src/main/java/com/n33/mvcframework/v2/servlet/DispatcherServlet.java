package com.n33.mvcframework.v2.servlet;

import com.n33.mvcframework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;

public class DispatcherServlet extends HttpServlet {
    /**
     * 保存application.properties 的配置
     */
    private Properties contextConfig = new Properties();

    /**
     * 保存扫描的所有的类名
     */
    private List<String> classNames = new ArrayList<>();

    /**
     * 模拟ioc
     */
    private Map<String, Object> ioc = new HashMap<>();

    /**
     * 保存url和Method的对应关系
     */
    private Map<String, Method> handlerMapping = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }

    }


    @Override
    public void init(ServletConfig config) throws ServletException {

        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2.扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //3.初始化扫描到的类，并将他们放到ioc容器中
        doInstance();

        //4.完成依赖注入
        doAutowired();

        //5.初始化HandlerMapping
        initHandlerMapping();

        System.out.println("N Spring framework is init.");

    }


    /**
     * 加载配置文件
     */
    private void doLoadConfig(String contextConfigLocation) {
        try (InputStream fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation)) {
            contextConfig.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource(File.separator + scanPackage.replaceAll("\\.", Matcher.quoteReplacement(File.separator)));
        for (File file : new File(url.getFile()).listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = scanPackage + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }

        }
    }

    private void doInstance() {
        //初始化，为DI做准备
        if (classNames.isEmpty()) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(NController.class)) {
                    Object instance = clazz.newInstance();
                    //默认首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(NService.class)) {
                    NService service = clazz.getAnnotation(NService.class);
                    String beanName = service.value();
                    if (beanName.trim().isEmpty()) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);

                    //根据类型自动赋值，取巧
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The \"" + i.getName() + "\" is exists!");
                        }
                        //接口名直接当key
                        ioc.put(i.getName(), instance);
                    }

                } else {
                    continue;
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //获取所有字段，包括private、protected、default
            //一般来说，OOP编程只能获取public
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(NAutowired.class)) {
                    continue;
                }
                NAutowired autowired = field.getAnnotation(NAutowired.class);

                //如果用户没有自定义beanName，则默认类型注入
                String beanName = autowired.value().trim();
                if (beanName.isEmpty()) {
                    beanName = field.getType().getName();
                }

                //如果是public以外的类型，只要有@Autowried注解都强制赋值
                //暴力访问
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //初始化url 和 Method 一对一关系
    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            if (!clazz.isAnnotationPresent(NController.class)) {
                continue;
            }

            //保存写在类上面的@RequestMapping注解value
            String baseUrl = "";
            if (clazz.isAnnotationPresent(NRequestMapping.class)) {
                NRequestMapping requestMapping = clazz.getAnnotation(NRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //默认获取所有public方法
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(NRequestMapping.class)) {
                    continue;
                }

                NRequestMapping requestMapping = method.getAnnotation(NRequestMapping.class);
                //优化
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);

                System.out.println("Mapped : " + url + "," + method);
            }
        }
    }


    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!");
            return;
        }

        Method method = this.handlerMapping.get(url);

        //获取方法额形参列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //保存请求的url参数列表
        Map<String, String[]> parameterMap = req.getParameterMap();
        //保存赋值参数额位置
        Object[] paramValues = new Object[parameterTypes.length];
        //根据参数位置动态赋值
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                paramValues[i] = req;
                continue;
            } else if (parameterType == HttpServletResponse.class) {
                paramValues[i] = resp;
                continue;
            } else if (parameterType == String.class) {
                //提取方法中加了注释的参数
                Annotation[][] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length; j++) {
                    for (Annotation a : pa[i]) {
                        if (a instanceof NRequestParam) {
                            String paramName = ((NRequestParam) a).value();
                            if (!paramName.trim().isEmpty()) {
                                String value = Arrays.toString(parameterMap.get(paramName))
                                        .replaceAll("\\[|\\]", "")
                                        .replaceAll("\\s", ",");
                                paramValues[i] = value;
                            }
                        }
                    }

                }
            }
            //还需要数字处理。。。以后再说
            else if (parameterType == Integer.class) {
                //提取方法中加了注释的参数
                Annotation[][] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length; j++) {
                    for (Annotation a : pa[i]) {
                        if (a instanceof NRequestParam) {
                            String paramName = ((NRequestParam) a).value();
                            if (!paramName.trim().isEmpty()) {
                                String value = Arrays.toString(parameterMap.get(paramName))
                                        .replaceAll("\\[|\\]", "")
                                        .replaceAll("\\s", ",");
                                paramValues[i] = value.isEmpty()?0:Integer.valueOf(value);
                            }
                        }
                    }

                }
            }
        }
        //取巧
        //通过反射获取Method所在的Class,获得Class之后还要获得Class的名称
        //再调用toLowerFirstCase获得beanName
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        //第一个参数：方法所在的实例
        //第二个参数：调用时所学参数
        method.invoke(ioc.get(beanName), paramValues);
    }


    private String toLowerFirstCase(String simpleName) {
        if (simpleName.trim().isEmpty())
            return "";
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
