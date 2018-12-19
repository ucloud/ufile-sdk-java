package cn.ucloud.ufile.util;

import cn.ucloud.ufile.annotation.UcloudParam;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/13 14:26
 */
public class ParameterMaker {

    public static <V> List<Parameter<V>> makeParameter(Object bean) throws IllegalAccessException, InvocationTargetException {
        List<Parameter<V>> params = new ArrayList<>();
        if (bean != null) {
            // 参数转化 1. 转化属性
            Class<?> objectClass = bean.getClass();
            Class<?> superclass = objectClass.getSuperclass();
            if (superclass != null) {
                params.addAll(getFieldParam(superclass, bean));
            }
            params.addAll(getFieldParam(objectClass, bean));
            // 参数转化 2. 方法转化
            if (superclass != null) {
                params.addAll(getMethodParam(superclass, bean));
            }
            params.addAll(getMethodParam(objectClass, bean));
        } else {
            throw new NullPointerException("params object can not be null");
        }
        return params;
    }

    /**
     * 根据类对象 获取属性参数
     *
     * @param clazz 类对象的class
     * @param bean  类对象
     * @return 属性参数列表
     * @throws Exception 不满足要求的参数  抛出异常
     */
    private static <V> List<Parameter<V>> getFieldParam(Class clazz, Object bean) throws IllegalAccessException {
        List<Parameter<V>> list = new ArrayList<>();
        if (clazz != null) {
            Field[] declaredFields = clazz.getDeclaredFields();
            int len = clazz.getDeclaredFields().length;
            for (int i = 0; i < len; i++) {
                UcloudParam annotation = declaredFields[i].getAnnotation(UcloudParam.class);
                if (annotation != null) {
                    declaredFields[i].setAccessible(true);
                    Object value = declaredFields[i].get(bean);
                    if (value != null) {
                        Parameter param = new Parameter(annotation.value(), value);
                        list.add(param);
                    }
                }
            }
        }
        return list;
    }


    /**
     * 根据类对象 获取方法参数
     *
     * @param clazz            类对象的class
     * @param bean 类对象
     * @return 方法参数列表
     * @throws Exception 对不满足要求的参数  抛出异常
     */
    private static <V> List<Parameter<V>> getMethodParam(Class clazz, Object bean) throws InvocationTargetException, IllegalAccessException {
        List<Parameter<V>> list = new ArrayList<>();
        if (clazz != null) {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            int len = declaredMethods.length;
            for (int i = 0; i < len; i++) {
                UcloudParam annotation = declaredMethods[i].getAnnotation(UcloudParam.class);
                if (annotation != null) {
                    declaredMethods[i].setAccessible(true);
                    Object invoke = declaredMethods[i].invoke(bean);
                    if (invoke != null && invoke instanceof List) {
                        List<Parameter<V>> params = (List<Parameter<V>>) invoke;
                        list.addAll(params);
                    }
                }
            }
        }

        return list;
    }
}
