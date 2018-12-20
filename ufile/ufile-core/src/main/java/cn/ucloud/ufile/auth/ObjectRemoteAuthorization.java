package cn.ucloud.ufile.auth;


import cn.ucloud.ufile.http.HttpClient;

/**
 * 远程授权者
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/6 18:50
 */
public abstract class ObjectRemoteAuthorization extends ObjectAuthorization {
    /**
     * 远程授权接口配置
     */
    protected ApiConfig apiConfig;

    /**
     * http客户端
     */
    protected HttpClient httpClient;

    /**
     * 构造方法
     *
     * @param publicKey 用户公钥
     * @param apiConfig 远程授权接口配置
     */
    public ObjectRemoteAuthorization(String publicKey, ApiConfig apiConfig) {
        super(publicKey);
        this.apiConfig = apiConfig;
    }

    public ObjectRemoteAuthorization setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public ApiConfig getApiConfig() {
        return apiConfig;
    }

    public static class ApiConfig {
        private String objectOptAuthServer;
        private String objectDownloadAuthServer;

        public ApiConfig(String objectOptAuthServer, String objectDownloadAuthServer) {
            this.objectOptAuthServer = objectOptAuthServer;
            this.objectDownloadAuthServer = objectDownloadAuthServer;
        }

        protected String getObjectOptAuthServer() {
            return objectOptAuthServer;
        }

        protected String getObjectDownloadAuthServer() {
            return objectDownloadAuthServer;
        }
    }
}
