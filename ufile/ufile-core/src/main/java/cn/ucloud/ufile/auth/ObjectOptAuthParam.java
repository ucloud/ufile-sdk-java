package cn.ucloud.ufile.auth;

import cn.ucloud.ufile.api.object.policy.PutPolicy;
import cn.ucloud.ufile.util.HttpMethod;

/**
 * @description:
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-19 10:08
 */
public class ObjectOptAuthParam extends ObjectBaseAuthParam {
    /**
     * 文件mimeType
     */
    private String contentType;
    /**
     * 文件MD5
     */
    private String contentMD5;
    /**
     * 日期（yyyyMMddHHmmss）
     */
    private String date;

    /**
     * X-UFile-Copy-Source
     */
    private String xUFileCopySource;

    /**
     * X-UFile-Copy-Source-Range
     */
    private String xUFileCopySourceRange;

    /**
     * 上传策略
     */
    private PutPolicy putPolicy;

    public ObjectOptAuthParam(HttpMethod method, String bucket, String keyName) {
        this(method, bucket, keyName, "", "", "");
    }

    public ObjectOptAuthParam(HttpMethod method, String bucket, String keyName, String contentType, String contentMD5, String date) {
        super(method, bucket, keyName);
        this.contentType = contentType;
        this.contentMD5 = contentMD5;
        this.date = date;
    }

    public ObjectOptAuthParam(HttpMethod method, String bucket, String keyName, String contentType, String contentMD5, String date, String xUFileCopySource, String xUFileCopySourceRange) {
        super(method, bucket, keyName);
        this.contentType = contentType;
        this.contentMD5 = contentMD5;
        this.date = date;
        this.xUFileCopySource = xUFileCopySource;
        this.xUFileCopySourceRange = xUFileCopySourceRange;
    }

    public String getContentType() {
        return contentType;
    }

    public ObjectOptAuthParam setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getContentMD5() {
        return contentMD5;
    }

    public ObjectOptAuthParam setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
        return this;
    }

    public String getDate() {
        return date;
    }

    public ObjectOptAuthParam setDate(String date) {
        this.date = date;
        return this;
    }

    public PutPolicy getPutPolicy() {
        return putPolicy;
    }

    public ObjectOptAuthParam setPutPolicy(PutPolicy putPolicy) {
        this.putPolicy = putPolicy;
        return this;
    }

    public String getXUFileCopySource() {
        return xUFileCopySource;
    }

    public ObjectOptAuthParam setXUFileCopySource(String xUFileCopySource) {
        this.xUFileCopySource = xUFileCopySource;
        return this;
    }

    public String getXUFileCopySourceRange() {
        return xUFileCopySourceRange;
    }

    public ObjectOptAuthParam setXUFileCopySourceRange(String xUFileCopySourceRange) {
        this.xUFileCopySourceRange = xUFileCopySourceRange;
        return this;
    }
}
