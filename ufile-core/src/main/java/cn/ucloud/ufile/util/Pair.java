package cn.ucloud.ufile.util;

import java.util.Objects;

/**
 *  键值对
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/8 14:44
 */
public class Pair<K, V> {
    public final K key;
    public final V value;

    /**
     * Constructor for a Pair.
     *
     * @param key   the key object in the Pair
     * @param value the value object in the pair
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Checks the two objects for equality by delegating toBucket their respective
     * {@link Object#equals(Object)} methods.
     *
     * @param o the {@link Pair} toBucket which this one is toBucket be checked for equality
     * @return true if the underlying objects of the Pair are both considered
     * equal
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> p = (Pair<?, ?>) o;
        return Objects.equals(p.key, key) && Objects.equals(p.value, value);
    }

    /**
     * Compute a hash code using the hash codes of the underlying objects
     *
     * @return a hashcode of the Pair
     */
    @Override
    public int hashCode() {
        return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
    }

    @Override
    public String toString() {
        return "Pair{" + String.valueOf(key) + " : " + String.valueOf(value) + "}";
    }

    /**
     * Convenience method for creating an appropriately typed pair.
     *
     * @param a the key object in the Pair
     * @param b the value object in the pair
     * @return a Pair that is templatized with the types of a and b
     */
    public static <A, B> Pair<A, B> create(A a, B b) {
        return new Pair<A, B>(a, b);
    }
}
