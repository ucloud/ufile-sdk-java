package cn.ucloud.ufile.http.interceptor;

import cn.ucloud.ufile.util.JLog;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * HTTP log 拦截器
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/9 14:17
 */
public class LogInterceptor implements Interceptor {
    private String TAG = getClass().getSimpleName();

    @Override
    public Response intercept(Chain chain) throws IOException {
        //获得请求信息，此处如有需要可以添加headers信息
        Request request = chain.request();

//        request.newBuilder().addHeader("Cookie", "aaaa");

        JLog.T(TAG, "[request]:" + request.toString());
        JLog.T(TAG, "[request-headers]:" + request.headers().toString());
//        JLog.T(TAG, "[request-body]:" + readRequestBody(request));

        /* 记录请求耗时 */
        long startNs = System.nanoTime();
        /* 发送请求，获得响应 */
        Response response = chain.proceed(request);

        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        /* 打印请求耗时 */
        JLog.D(TAG, "[耗时]:" + tookMs + "ms");
        /* 使用response获得headers(),可以更新本地Cookie。*/
        JLog.T(TAG, "[response-code]:" + response.code());
        JLog.T(TAG, "[response-headers]:" + response.headers().toString());

        /* 获得返回的body，注意此处不要使用responseBody.string()获取返回数据，原因在于这个方法会消耗返回结果的数据(buffer) */
        ResponseBody responseBody = response.body();
        /* 为了不消耗buffer，我们这里使用source先获得buffer对象，然后clone()后使用 */
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        /* 获得返回的数据 */
        Buffer buffer = source.buffer();
        if (buffer.size() < 1024)
            /* 使用前clone() 下，避免直接消耗 */
            JLog.T(TAG, "[response-body]:" + buffer.clone().readString(Charset.forName("UTF-8")));

        return response;
    }

    private String readRequestBody(Request oriReq) {
        if (oriReq.body() == null)
            return "";

        Request request = oriReq.newBuilder().build();
        Buffer buffer = new Buffer();
        try {
            request.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
