package cn.ucloud.ufile.util;

import cn.ucloud.ufile.compat.base64.Base64UrlEncoderCompat;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import cn.ucloud.ufile.UfileConstants;

import java.io.*;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/19 14:58
 */
public class Etag {
    private static final String TAG = "Etag";

    /**
     * 子分片的ETag列表
     */
    @SerializedName("PartEtags")
    private List<String> partEtags;
    /**
     * ETag值
     */
    @SerializedName("ETag")
    private String eTag;

    Etag() {
        partEtags = new ArrayList<>();
    }

    public List<String> getPartEtags() {
        return partEtags;
    }

    public String geteTag() {
        return eTag;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    /**
     * 计算ETag，计算ETag分片的大小默认使用{@link UfileConstants}指定的大小，4 MB
     *
     * @param file   待计算ETag的文件
     * @param base64 Base64 UrlEncoder兼容器 {@link Base64UrlEncoderCompat}
     * @return ETag对象
     * @throws IOException
     */
    public static Etag etag(File file, Base64UrlEncoderCompat base64) throws IOException {
        return etag(file, UfileConstants.MULTIPART_SIZE, base64);
    }


    /**
     * 计算ETag，计算ETag分片的大小默认使用{@link UfileConstants}指定的大小，4 MB
     *
     * @param inputStream 待计算ETag的流
     * @param base64      Base64 UrlEncoder兼容器 {@link Base64UrlEncoderCompat}
     * @return ETag对象
     * @throws IOException
     */
    public static Etag etag(InputStream inputStream, Base64UrlEncoderCompat base64) throws IOException {
        return etag(inputStream, UfileConstants.MULTIPART_SIZE, base64);
    }

    /**
     * 计算ETag
     *
     * @param file     待计算ETag的文件
     * @param partSize 计算ETag分片的大小
     * @param base64   Base64 UrlEncoder兼容器 {@link Base64UrlEncoderCompat}
     * @return ETag对象
     * @throws IOException
     */
    public static Etag etag(File file, int partSize, Base64UrlEncoderCompat base64) throws IOException {
        if (file == null || !file.exists() || !file.isFile())
            return null;

        return etag(new FileInputStream(file), partSize, base64);
    }

    /**
     * 计算ETag
     *
     * @param inputStream 待计算ETag的流
     * @param partSize    计算ETag分片的大小
     * @param base64      Base64 UrlEncoder兼容器 {@link Base64UrlEncoderCompat}
     * @return ETag对象
     * @throws IOException
     */
    public static Etag etag(InputStream inputStream, int partSize, Base64UrlEncoderCompat base64) throws IOException {
        Etag eTag = new Etag();
        if (inputStream == null || partSize <= 0)
            return null;

        try {
            long size = inputStream.available();
            int blockCount = (int) Math.ceil(size * 1.d / partSize);
            byte[] head = NumberUtil.getBytes(blockCount, ByteOrder.LITTLE_ENDIAN);

            int headSize = head.length;
            byte[] buff = new byte[headSize + 20];
            System.arraycopy(head, 0, buff, 0, headSize);

            byte[] cache = new byte[partSize];
            int readLen = 0;

            byte[] sha1Res = null;
            MessageDigest digest = MessageDigest.getInstance(Encryptor.TYPE_SHA1);

            if (blockCount > 1) {
                MessageDigest digestSub = MessageDigest.getInstance(Encryptor.TYPE_SHA1);
                for (int i = 0; i < blockCount; i++) {
                    if ((readLen = inputStream.read(cache)) > 0) {
                        digestSub.update(cache, 0, readLen);
                    } else {
                        break;
                    }
                    byte[] tmp = digestSub.digest();
                    eTag.partEtags.add(base64.urlEncodeToString(tmp));
                    digest.update(tmp);
                }
            } else {
                if ((readLen = inputStream.read(cache)) > 0) {
                    digest.update(cache, 0, readLen);
                }
            }

            sha1Res = digest.digest();
            System.arraycopy(sha1Res, 0, buff, 4, sha1Res.length);
            eTag.eTag = base64.urlEncodeToString(buff);
            return eTag;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(inputStream);
        }

        return null;
    }
}
