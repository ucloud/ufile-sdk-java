package cn.ucloud.ufile.auth;

import cn.ucloud.ufile.util.HttpMethod;
import com.google.gson.JsonElement;

/**
 * @description:
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-19 10:16
 */
public class ObjectBaseAuthParam {
    /**
     * Http请求方式 {@link HttpMethod}
     */
    private HttpMethod method;
    /**
     * bucket名称
     */
    private String bucket;
    /**
     * 文件keyName
     */
    private String keyName;
    /**
     * 用户可选参数
     */
    private JsonElement optional;

    public ObjectBaseAuthParam(HttpMethod method, String bucket, String keyName) {
        this.method = method;
        this.bucket = bucket;
        this.keyName = keyName;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public ObjectBaseAuthParam setMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public String getBucket() {
        return bucket;
    }

    public ObjectBaseAuthParam setBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }

    public String getKeyName() {
        return keyName;
    }

    public ObjectBaseAuthParam setKeyName(String keyName) {
        this.keyName = keyName;
        return this;
    }

    public JsonElement getOptional() {
        return optional;
    }

    public ObjectBaseAuthParam setOptional(JsonElement optional) {
        this.optional = optional;
        return this;
    }
}
