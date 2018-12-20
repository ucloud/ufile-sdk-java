package cn.ucloud.ufile.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  UCloud Server request参数注解，使用{@link cn.ucloud.ufile.util.ParameterMaker}序列化出指定的parameter键值对
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/13 14:32
 */

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UcloudParam {
    /**
     * 参数Key值
     * @return
     */
    String value();
}
