package cn.ucloud.ufile.util;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/7 15:34
 */
public enum HttpMethod {
    GET("GET"),
    POST("POST"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS"),
    PUT("PUT"),
    DELETE("DELETE"),
    TRACE("TRACE");

    private final String name;

    HttpMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
