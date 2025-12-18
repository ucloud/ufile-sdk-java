package cn.ucloud.ufile.auth;

import cn.ucloud.ufile.util.HttpMethod;

/**
 * @description:
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-19 10:08
 */
public class ObjectDownloadAuthParam extends ObjectBaseAuthParam {
    /**
     * 过期时间 (当前时间加上一个有效时间, 单位：Unix time second)
     */
    private long expires;

    /**
     * Optional: for presigned PUT/POST, some server implementations include Content-Type in signature.
     */
    private String contentType;

    /**
     * Optional: for presigned PUT/POST, some server implementations include Content-MD5 in signature.
     */
    private String contentMD5;

    public ObjectDownloadAuthParam(HttpMethod method, String bucket, String keyName) {
        this(method, bucket, keyName, -1);
    }

    public ObjectDownloadAuthParam(HttpMethod method, String bucket, String keyName, long expires) {
        super(method, bucket, keyName);
        this.expires = expires;
    }

    public long getExpires() {
        return expires;
    }

    public ObjectDownloadAuthParam setExpires(long expires) {
        this.expires = expires;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public ObjectDownloadAuthParam setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getContentMD5() {
        return contentMD5;
    }

    public ObjectDownloadAuthParam setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
        return this;
    }
}
