package cn.ucloud.ufile.util;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/8 10:57
 */
public class Encryptor {
    public final static String TYPE_HMAC_SHA1 = "HmacSHA1";
    public final static String TYPE_SHA1 = "SHA1";

    /**
     * Hmac-SHA1 加密
     *
     * @param key  加密秘钥
     * @param data 加密内容
     * @return 加密结果
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws IllegalStateException
     */
    public static byte[] Hmac_SHA1(byte[] key, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException {
        if (key == null || key.length == 0)
            return null;

        if (data == null || data.length == 0)
            return null;

        byte[] result = null;
        SecretKey secret = new SecretKeySpec(key, TYPE_HMAC_SHA1);
        Mac mac = Mac.getInstance(TYPE_HMAC_SHA1);
        mac.init(secret);
        result = mac.doFinal(data);

        return result;
    }

    /**
     * Hmac-SHA1 加密
     *
     * @param key  加密秘钥
     * @param data 加密内容
     * @return 加密结果
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws IllegalStateException
     */
    public static byte[] Hmac_SHA1(String key, String data) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, IllegalStateException {
        if (key == null || key.length() == 0)
            return null;

        if (data == null || data.length() == 0)
            return null;

        return Hmac_SHA1(key.getBytes("UTF-8"), data.getBytes("UTF-8"));
    }

    /**
     * SHA1 加密
     *
     * @param data 加密内容
     * @return 加密结果
     * @throws NoSuchAlgorithmException
     */
    public static byte[] SHA1(byte[] data) throws NoSuchAlgorithmException {
        if (data == null || data.length == 0)
            return null;

        MessageDigest digest = MessageDigest.getInstance(TYPE_SHA1);
        return digest.digest(data);
    }

    public static byte[] SHA1(File file) throws NoSuchAlgorithmException, IOException {
        if (!file.exists() || !file.isFile())
            return null;

        FileInputStream fis = null;
        return SHA1(new FileInputStream(file));
    }

    /**
     * 对数据流的SHA1 加密
     *
     * @param inputStream 待加密的数据流
     * @return 加密结果
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static byte[] SHA1(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        if (inputStream == null)
            return null;

        MessageDigest sha1 = MessageDigest.getInstance(TYPE_SHA1);
        try {
            byte[] buff = new byte[64 * 1024];
            int len = 0;
            while ((len = inputStream.read(buff)) > 0)
                sha1.update(buff, 0, len);
        } finally {
            FileUtil.close(inputStream);
        }

        return sha1.digest();
    }
}
