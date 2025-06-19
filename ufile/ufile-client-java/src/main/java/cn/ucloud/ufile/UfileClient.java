package cn.ucloud.ufile;

import cn.ucloud.ufile.auth.BucketAuthorizer;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.api.bucket.BucketApiBuilder;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.ObjectApiBuilder;
import cn.ucloud.ufile.http.HttpClient;

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
    private String securityToken;

    private Config config;

    private UfileClient() {
        this(new Config());
    }

    private UfileClient(Config config) {
        if (config == null) {
            config = new Config();
        }
        this.config = config;
        this.httpClient = new HttpClient(config.httpClientConfig);
    }

    private static UfileClient createClient() {
        if (mInstance == null) {
            synchronized (UfileClient.class) {
                if (mInstance == null) {
                    mInstance = new UfileClient();
                }
            }
        }

        return mInstance;
    }

    private static UfileClient createClient(Config config) {
        if (mInstance == null) {
            synchronized (UfileClient.class) {
                if (mInstance == null) {
                    mInstance = new UfileClient(config);
                }
            }
        }

        return mInstance;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * 设置安全令牌（STS临时凭证）
     *
     * @param securityToken 安全令牌
     * @return UfileClient实例{@link UfileClient}
     */
    public synchronized static UfileClient withSecurityToken(String securityToken) {
        UfileClient client = createClient();
        client.securityToken = securityToken;
        return client;
    }

    /**
     * 配置类，为静态内部类，可以在外部类构造函数中引用
     */
    public static class Config {
        private HttpClient.Config httpClientConfig;

        public Config() {
            this(new HttpClient.Config());
        }

        public Config(HttpClient.Config httpClientConfig) {
            this.httpClientConfig = httpClientConfig;
        }

        public HttpClient.Config getHttpClientConfig() {
            return httpClientConfig;
        }

        public Config setHttpClientConfig(HttpClient.Config httpClientConfig) {
            this.httpClientConfig = httpClientConfig;
            return this;
        }
    }

    /**
     * 配置并构建UfileClient
     * 必须在调用bucket()和object()之前调用，否则无效
     *
     * @param config {@link Config}
     * @return UfileClient实例{@link UfileClient}
     */
    public synchronized static UfileClient configure(Config config) {
        return createClient(config);
    }

    /**
     * 获取UfileClient配置项
     *
     * @return UfileClient配置项 {@link Config}
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Bucket系列API
     *
     * @param authorizer Bucket相关API授权者{@link BucketAuthorizer}
     * @return Bucket相关API构造器 {@link BucketApiBuilder}
     */
    public synchronized static BucketApiBuilder bucket(BucketAuthorizer authorizer) {
        UfileClient client = createClient();
        BucketApiBuilder builder = new BucketApiBuilder(client, authorizer);
        return builder;
    }

    /**
     * Object系列API
     *
     * @param authorizer Object相关API授权者{@link ObjectAuthorizer}
     * @param config     Object相关API配置选项
     * @return Object相关API构造器 {@link ObjectApiBuilder}
     */
    public synchronized static ObjectApiBuilder object(ObjectAuthorizer authorizer, ObjectConfig config) {
        UfileClient client = createClient();
        ObjectApiBuilder builder = new ObjectApiBuilder(client, authorizer, config);
        
        
        if (client.securityToken != null && !client.securityToken.isEmpty()) {
            builder.withSecurityToken(client.securityToken);
        }
        
        return builder;
    }
}
