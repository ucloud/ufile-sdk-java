package cn.ucloud.ufile.http;

import cn.ucloud.ufile.http.interceptor.LogInterceptor;
import okhttp3.OkHttpClient;

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
    public static final long DEFAULT_CONNECT_TIMEOUT = 5 * 1000;
    public static final long DEFAULT_WRITE_TIMEOUT = 15 * 1000;
    public static final long DEFAULT_READ_TIMEOUT = 15 * 1000;

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

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }
}
