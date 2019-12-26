package cn.ucloud.ufile.sample;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.auth.*;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.interceptor.LogInterceptor;
import cn.ucloud.ufile.util.JLog;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 16:13
 */
public class Constants {
    static {
        /**
         * 开启Debug级别日志
         */
        JLog.SHOW_TEST = true;
        JLog.SHOW_DEBUG = true;

        /**
         * 配置UfileClient，必须在使用UfileClient之前调用
         */
        UfileClient.configure(new UfileClient.Config(
                new HttpClient.Config(10, 5, TimeUnit.MINUTES)
                        .setTimeout(10 * 1000, 30 * 1000, 30 * 1000)
                        .setExecutorService(Executors.newSingleThreadExecutor())));
    }

    /**
     * 本地Bucket相关API的签名器（账号在ucloud 的API 公私钥，不能使用token）
     * 如果只用到了文件操作，不需要配置下面的bucket 操作公私钥
     */
    public static final BucketAuthorization BUCKET_AUTHORIZER = new UfileBucketLocalAuthorization(
            "UcloudPublicKey",
            "UcloudPrivateKey");

    /**
     * 本地Object相关API的签名器
     * 请修改下面的公私钥
     */
    public static final ObjectAuthorization OBJECT_AUTHORIZER = new UfileObjectLocalAuthorization(
            "UcloudPublicKey",
            "UcloudPrivateKey");

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
