package cn.ucloud.ufile.compat.base64;


import cn.ucloud.ufile.util.Base64;

/**
 * @description:
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2019-03-26 20:42
 */
@Deprecated
public class DefaultBase64UrlEncoderCompat implements Base64UrlEncoderCompat {
    @Override
    public byte[] urlEncode(byte[] src) {
        return Base64.getUrlEncoder().encode(src);
    }

    @Override
    public String urlEncodeToString(byte[] src) {
        return Base64.getUrlEncoder().encodeToString(src);
    }
}
