package cn.ucloud.ufile.http;

import cn.ucloud.ufile.http.interceptor.LogInterceptor;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Ufile Http 管理器
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/9 11:48
 */
public class HttpClient {
    protected String TAG = getClass().getSimpleName();
    @Deprecated
    public static final long DEFAULT_CONNECT_TIMEOUT = 10 * 1000;
    @Deprecated
    public static final long DEFAULT_WRITE_TIMEOUT = 30 * 1000;
    @Deprecated
    public static final long DEFAULT_READ_TIMEOUT = 30 * 1000;

    /**
     * 原始OkHttpClient，全局保持唯一一个，从而保证性能开销
     */
    private OkHttpClient mOkHttpClient = null;

    /**
     * httpClient的配置选项 {@link Config}
     */
    private Config config;

    public static class Config {
        public static final long DEFAULT_CONNECT_TIMEOUT = 10 * 1000;
        public static final long DEFAULT_WRITE_TIMEOUT = 30 * 1000;
        public static final long DEFAULT_READ_TIMEOUT = 30 * 1000;
        /**
         * 默认okhttp最大空闲连接数（5）
         */
        public static final int DEFAULT_MAX_IDLE_CONNECTIONS = 5;
        /**
         * 默认okhttp活动链接存货时间（5分钟）
         */
        public static final long DEFAULT_KEEP_ALIVE_DURATION_MINUTES = 5;
        /**
         * 默认okhttp活动链接存货时间单位, （分钟）
         */
        public static final TimeUnit DEFAULT_KEEP_ALIVE_DURATION_TIME_UNIT = TimeUnit.MINUTES;

        private long timeoutConnect;
        private long timeoutRead;
        private long timeoutWrite;
        private int maxIdleConnections;
        private long keepAliveDuration;
        private TimeUnit keepAliveTimeUnit;
        private List<Interceptor> interceptors;
        private List<Interceptor> networkInterceptors;
        private ExecutorService executorService;

        public Config() {
            this(DEFAULT_MAX_IDLE_CONNECTIONS, DEFAULT_KEEP_ALIVE_DURATION_MINUTES, DEFAULT_KEEP_ALIVE_DURATION_TIME_UNIT);
        }

        public Config(int maxIdleConnections, long keepAliveDuration, TimeUnit keepAliveTimeUnit) {
            this.maxIdleConnections = maxIdleConnections;
            this.keepAliveDuration = keepAliveDuration;
            this.keepAliveTimeUnit = keepAliveTimeUnit;
            this.timeoutConnect = DEFAULT_CONNECT_TIMEOUT;
            this.timeoutRead = DEFAULT_READ_TIMEOUT;
            this.timeoutWrite = DEFAULT_WRITE_TIMEOUT;
            this.interceptors = new ArrayList<>();
            this.interceptors.add(new LogInterceptor());
        }

        public Config setInterceptors(List<Interceptor> interceptors) {
            this.interceptors = interceptors;
            return this;
        }

        public Config addInterceptor(Interceptor interceptor) {
            if (interceptor == null)
                return this;

            if (this.interceptors == null)
                this.interceptors = new ArrayList<>();

            this.interceptors.add(interceptor);
            return this;
        }

        public Config setNetworkInterceptors(List<Interceptor> networkInterceptors) {
            this.networkInterceptors = networkInterceptors;
            return this;
        }

        public Config addNetInterceptor(Interceptor interceptor) {
            if (interceptor == null)
                return this;

            if (this.networkInterceptors == null)
                this.networkInterceptors = new ArrayList<>();

            this.networkInterceptors.add(interceptor);
            return this;
        }

        public Config setMaxIdleConnections(int maxIdleConnections) {
            this.maxIdleConnections = maxIdleConnections;
            return this;
        }

        public Config setKeepAliveDuration(long keepAliveDuration) {
            this.keepAliveDuration = keepAliveDuration;
            return this;
        }

        public Config setKeepAliveTimeUnit(TimeUnit keepAliveTimeUnit) {
            this.keepAliveTimeUnit = keepAliveTimeUnit;
            return this;
        }

        public Config setExecutorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public Config setTimeout(long timeoutConnect, long timeoutRead, long timeoutWrite) {
            this.timeoutConnect = timeoutConnect;
            this.timeoutRead = timeoutRead;
            this.timeoutWrite = timeoutWrite;
            return this;
        }

        public long getTimeoutConnect() {
            return timeoutConnect;
        }

        public long getTimeoutRead() {
            return timeoutRead;
        }

        public long getTimeoutWrite() {
            return timeoutWrite;
        }

        public ExecutorService getExecutorService() {
            return executorService;
        }

        public int getMaxIdleConnections() {
            return maxIdleConnections;
        }

        public long getKeepAliveDuration() {
            return keepAliveDuration;
        }

        public TimeUnit getKeepAliveTimeUnit() {
            return keepAliveTimeUnit;
        }

        public List<Interceptor> getInterceptors() {
            return interceptors;
        }

        public List<Interceptor> getNetworkInterceptors() {
            return networkInterceptors;
        }
    }

    /**
     * 构造方法
     */
    public HttpClient() {
        this(new Config());
    }

    /**
     * 构造方法
     *
     * @param config {@link Config}
     */
    public HttpClient(Config config) {
        if (config == null)
            config = new Config();

        this.config = config;

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.timeoutConnect, TimeUnit.MILLISECONDS)
                .writeTimeout(config.timeoutWrite, TimeUnit.MILLISECONDS)
                .readTimeout(config.timeoutRead, TimeUnit.MILLISECONDS)
                .connectionPool(new ConnectionPool(config.maxIdleConnections, config.keepAliveDuration,
                        config.keepAliveTimeUnit));

        if (config.interceptors != null) {
            for (Interceptor interceptor : config.interceptors) {
                if (interceptor == null)
                    continue;
                builder.addInterceptor(interceptor);
            }
        }
        if (config.networkInterceptors != null) {
            for (Interceptor interceptor : config.networkInterceptors) {
                if (interceptor == null)
                    continue;
                builder.addNetworkInterceptor(interceptor);
            }
        }
        if (config.executorService != null) {
            builder.dispatcher(new Dispatcher(config.executorService));
        }

        mOkHttpClient = builder.build();
    }


    /**
     * 获取 HttpClient异步线程池
     *
     * @return HttpClient异步线程池
     */
    @Deprecated
    public synchronized ExecutorService getExecutorService() {
        return this.mOkHttpClient.dispatcher().executorService();
    }

    public Config getConfig() {
        return config;
    }

    /**
     * 重新配置HttpClient
     *
     * @param config {@link Config}
     */
    public synchronized void resetHttpClient(Config config) {
        if (config == null)
            return;

        this.config = config;

        OkHttpClient.Builder builder = mOkHttpClient.newBuilder()
                .connectTimeout(config.timeoutConnect, TimeUnit.MILLISECONDS)
                .writeTimeout(config.timeoutWrite, TimeUnit.MILLISECONDS)
                .readTimeout(config.timeoutRead, TimeUnit.MILLISECONDS)
                .connectionPool(new ConnectionPool(config.maxIdleConnections, config.keepAliveDuration,
                        config.keepAliveTimeUnit));

        if (builder.interceptors() != null)
            builder.interceptors().clear();

        if (config.interceptors != null) {
            for (Interceptor interceptor : config.interceptors) {
                if (interceptor == null)
                    continue;
                builder.addInterceptor(interceptor);
            }
        }

        if (builder.networkInterceptors() != null)
            builder.networkInterceptors().clear();

        if (config.networkInterceptors != null) {
            for (Interceptor interceptor : config.networkInterceptors) {
                if (interceptor == null)
                    continue;
                builder.addNetworkInterceptor(interceptor);
            }
        }
        if (config.executorService != null) {
            builder.dispatcher(new Dispatcher(config.executorService));
        }

        mOkHttpClient = builder.build();

        this.mOkHttpClient = builder.build();
    }

    public synchronized OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }
}
