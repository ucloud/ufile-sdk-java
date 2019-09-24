package cn.ucloud.ufile.api.object.policy;


import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.util.JLog;

/**
 * @description: Put上传回调策略
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2019/9/19 14:13
 */
public abstract class PutPolicy {
    protected static final String TAG = "PutPolicy";

    protected String policy;

    PutPolicy() {
        this.policy = "";
    }

    PutPolicy(String policy) throws UfileClientException {
        JLog.D(TAG, "[PutPolicy]:" + policy);
        if (policy == null || policy.isEmpty())
            throw new UfileClientException("PutPolicy can not be null or empty");
        if (policy.length() > 4 << 10)
            throw new UfileClientException("Your PutPolicy is too long after Base64 encoding, max length is 4096 Bytes");

        this.policy = policy;
    }

    static abstract class Builder<T extends PutPolicy> {
        public abstract T build() throws UfileClientException;
    }

    public String getPolicy() {
        return policy;
    }
}
