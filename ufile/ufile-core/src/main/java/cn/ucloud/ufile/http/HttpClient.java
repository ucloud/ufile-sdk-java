package cn.ucloud.ufile.http;

import cn.ucloud.ufile.http.interceptor.LogInterceptor;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

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
    public static final long DEFAULT_CONNECT_TIMEOUT = 10 * 1000;
    public static final long DEFAULT_WRITE_TIMEOUT = 30 * 1000;
    public static final long DEFAULT_READ_TIMEOUT = 30 * 1000;

    /**
     * 原始OkHttpClient，全局保持唯一一个，从而保证性能开销
     */
    private OkHttpClient mOkHttpClient = null;

    /**
     * 构造方法
     */
    public HttpClient() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new LogInterceptor())
                .build();
    }

    /**
     * 获取 HttpClient异步线程池
     *
     * @return HttpClient异步线程池
     */
    public ExecutorService getExecutorService() {
        return this.mOkHttpClient.dispatcher().executorService();
    }

    /**
     * 设置HttpClient异步线程池
     *
     * @param executorService HttpClient异步线程池
     */
    public void setExecutorService(ExecutorService executorService) {
        this.mOkHttpClient = mOkHttpClient.newBuilder()
                .dispatcher(new Dispatcher(executorService))
                .build();
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }
}
