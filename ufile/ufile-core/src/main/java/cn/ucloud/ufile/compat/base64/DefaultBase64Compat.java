package cn.ucloud.ufile.compat.base64;

import java.util.Base64;

/**
 * @description:
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2019-03-26 20:42
 */
public class DefaultBase64Compat implements Base64Compat {
    @Override
    public byte[] decode(byte[] src) {
        return Base64.getDecoder().decode(src);
    }

    @Override
    public byte[] decode(String src) {
        return Base64.getDecoder().decode(src);
    }

    @Override
    public byte[] encode(byte[] src) {
        return Base64.getEncoder().encode(src);
    }

    @Override
    public String encodeToString(byte[] src) {
        return Base64.getEncoder().encodeToString(src);
    }

    @Override
    public byte[] urlDecode(byte[] src) {
        return Base64.getUrlDecoder().decode(src);
    }

    @Override
    public byte[] urlDecode(String src) {
        return Base64.getUrlDecoder().decode(src);
    }

    @Override
    public byte[] urlEncode(byte[] src) {
        return Base64.getUrlEncoder().encode(src);
    }

    @Override
    public String urlEncodeToString(byte[] src) {
        return Base64.getUrlEncoder().encodeToString(src);
    }
}
