package cn.ucloud.ufile.util;

import java.io.*;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 编码器
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/7 17:29
 */
public class Encoder {
    public final static String TYPE_MD5 = "MD5";

    /**
     * MD5 编码
     *
     * @param data 待计算MD5的数据
     * @return MD5值
     * @throws NoSuchAlgorithmException
     */
    public static byte[] md5(byte[] data) throws NoSuchAlgorithmException {
        if (data == null || data.length == 0)
            return null;

        MessageDigest md5 = MessageDigest.getInstance(TYPE_MD5);
        return md5.digest(data);
    }

    /**
     * 文件MD5 编码
     *
     * @param file 待计算MD5的文件
     * @return MD5值
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static byte[] md5(File file) throws NoSuchAlgorithmException, IOException {
        if (!file.exists() || !file.isFile())
            return null;

        return md5(new FileInputStream(file));
    }

    /**
     * 流MD5 编码
     *
     * @param inputStream 待计算MD5的输入流
     * @return MD5值
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static byte[] md5(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        if (inputStream == null)
            return null;

        MessageDigest md5 = MessageDigest.getInstance(TYPE_MD5);
        try {
            byte[] buff = new byte[64 * 1024];
            int len = 0;
            while ((len = inputStream.read(buff)) > 0)
                md5.update(buff, 0, len);
        } finally {
            FileUtil.close(inputStream);
        }

        return md5.digest();
    }

    public static String urlEncode(String src, String enc) throws UnsupportedEncodingException {
        if (src == null || src.isEmpty())
            return "";

        return URLEncoder.encode(src, enc).replace("+", "%20");
    }

    /**
     * URL-encode a path while preserving '/' separators.
     *
     * <p>Note: {@link URLEncoder} is designed for query-string encoding and will encode '/' as "%2F".
     * For object keys that use '/' as a virtual directory separator, we must keep '/' unescaped in the
     * request URL path; otherwise signature verification may fail (presigned URL) and some gateways
     * may reject encoded slashes.</p>
     */
    public static String urlEncodePath(String path, String enc) throws UnsupportedEncodingException {
        if (path == null || path.isEmpty()) {
            return "";
        }

        // Keep empty segments (split with -1) to preserve leading/trailing '/' if present.
        String[] parts = path.split("/", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append('/');
            String part = parts[i];
            if (part != null && !part.isEmpty()) {
                sb.append(urlEncode(part, enc));
            }
        }
        return sb.toString();
    }
}
