package cn.ucloud.ufile.util;

/**
 * @description:
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2019/9/19 14:47
 */
public abstract class Param<K,V> {
    protected K key;
    protected V value;

    public Param(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    protected abstract String format();
}
