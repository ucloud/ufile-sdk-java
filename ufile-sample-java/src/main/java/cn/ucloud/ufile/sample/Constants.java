package cn.ucloud.ufile.sample;

import cn.ucloud.ufile.auth.*;
import cn.ucloud.ufile.util.JLog;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 16:13
 */
public class Constants {
    static {
        // 开启Debug级别日志
        JLog.SHOW_TEST = true;
        JLog.SHOW_DEBUG = true;
    }

    /**
     * 本地Bucket相关API的签名器
     */
    public static final BucketAuthorization BUCKET_AUTHORIZER = new UfileBucketLocalAuthorization(
            System.getenv("UcloudPublicKey"),
            System.getenv("UcloudPrivateKey"));

    /**
     * 本地Object相关API的签名器
     */
    public static final ObjectAuthorization OBJECT_AUTHORIZER = new UfileObjectLocalAuthorization(
            System.getenv("UcloudPublicKey"),
            System.getenv("UcloudPrivateKey"));

    /**
     * 远程Object相关API的签名器
     */
//    public static final ObjectAuthorization OBJECT_AUTHORIZER = new UfileObjectRemoteAuthorization(
//            您的公钥,
//            new ObjectRemoteAuthorization.ApiConfig(
//                    "http://your_domain/applyAuth",
//                    "http://your_domain/applyPrivateUrlAuth"
//            ));
}
