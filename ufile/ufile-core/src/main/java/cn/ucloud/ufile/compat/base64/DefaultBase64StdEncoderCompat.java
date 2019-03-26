package cn.ucloud.ufile.compat.base64;

import java.util.Base64;

/**
 * @description:
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2019-03-26 20:42
 */
public class DefaultBase64StdEncoderCompat implements Base64StdEncoderCompat {

    @Override
    public byte[] encode(byte[] src) {
        return Base64.getEncoder().encode(src);
    }

    @Override
    public String encodeToString(byte[] src) {
        return Base64.getEncoder().encodeToString(src);
    }
}
