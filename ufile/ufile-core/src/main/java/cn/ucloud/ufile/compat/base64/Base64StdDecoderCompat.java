package cn.ucloud.ufile.compat.base64;

/**
 * @description: SDK开发环境是Java 1.8，而Base64类 since Java 1.8。本类的目的是为了兼容低版本Java环境使用Base64
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2019-03-26 19:00
 */
@Deprecated
public interface Base64StdDecoderCompat {
    /**
     * Base64 标准解码
     * SDK内部凡是使用到该方法的部分，都需要确保其结果和 Java 1.8中 Base64.getDecoder().decode(byte[]) 一致
     *
     * @param src 源byte
     * @return
     */
    byte[] decode(byte[] src);

    /**
     * Base64 标准解码
     * SDK内部凡是使用到该方法的部分，都需要确保其结果和 Java 1.8中 Base64.getDecoder().decode(String) 一致
     *
     * @param src 源byte
     * @return
     */
    byte[] decode(String src);
}
