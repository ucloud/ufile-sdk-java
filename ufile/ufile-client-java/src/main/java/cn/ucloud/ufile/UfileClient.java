package cn.ucloud.ufile;

import cn.ucloud.ufile.auth.BucketAuthorizer;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.api.bucket.BucketApiBuilder;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.ObjectApiBuilder;
import cn.ucloud.ufile.http.HttpClient;

import java.util.concurrent.ExecutorService;

/**
 * Ufile SDK
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/9 17:08
 */
public class UfileClient {
    private static volatile UfileClient mInstance;
    private HttpClient httpClient;
    private ExecutorService executorService;

    private UfileClient() {
        this.httpClient = new HttpClient();
    }

    private static UfileClient createClient() {
        if (mInstance == null) {
            synchronized (UfileClient.class) {
                if (mInstance == null)
                    mInstance = new UfileClient();
            }
        }

        return mInstance;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * 设置HttpClient异步线程池
     *
     * @param executorService HttpClient异步线程池
     */
    public static void setExecutorService(ExecutorService executorService) {
        if (mInstance != null)
            mInstance.httpClient.setExecutorService(executorService);
    }

    /**
     * 获取HttpClient异步线程池
     *
     * @return HttpClient异步线程池
     */
    public static ExecutorService getExecutorService() {
        if (mInstance == null)
            return null;
        return mInstance.httpClient.getExecutorService();
    }

    /**
     * Bucket系列API
     *
     * @param authorizer Bucket相关API授权者{@link BucketAuthorizer}
     * @return Bucket相关API构造器 {@link BucketApiBuilder}
     */
    public static BucketApiBuilder bucket(BucketAuthorizer authorizer) {
        return new BucketApiBuilder(createClient(), authorizer);
    }

    /**
     * Object系列API
     *
     * @param authorizer Object相关API授权者{@link ObjectAuthorizer}
     * @param config     Object相关API配置选项
     * @return Object相关API构造器 {@link ObjectApiBuilder}
     */
    public static ObjectApiBuilder object(ObjectAuthorizer authorizer, ObjectConfig config) {
        return new ObjectApiBuilder(createClient(), authorizer, config.host());
    }
}
