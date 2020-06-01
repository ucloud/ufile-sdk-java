package cn.ucloud.ufile.util;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/8 15:04
 */
public class Parameter<T> {
    public final String key;
    public final T value;

    public Parameter(String key, T value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("[%s]: %s", key, String.valueOf(value));
    }
}
