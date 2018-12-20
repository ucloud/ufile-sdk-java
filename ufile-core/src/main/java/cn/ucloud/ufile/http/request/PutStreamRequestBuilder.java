package cn.ucloud.ufile.http.request;

import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.ProgressConfig;
import cn.ucloud.ufile.http.request.body.UploadStreamRequestBody;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Http PUT<Stream> 请求构造器
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/9 11:55
 */
public class PutStreamRequestBuilder extends HttpRequestBuilder<InputStream> {
    private OnProgressListener onProgressListener;
    private ProgressConfig progressConfig;

    public PutStreamRequestBuilder(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    public PutStreamRequestBuilder setProgressConfig(ProgressConfig progressConfig) {
        this.progressConfig = progressConfig;
        return this;
    }

    @Override
    public Call build(OkHttpClient httpClient) {
        UploadStreamRequestBody requestBody = (UploadStreamRequestBody) new UploadStreamRequestBody(params, mediaType,
                Long.parseLong(header.getOrDefault("Content-Length", "0")), onProgressListener)
                .setProgressConfig(progressConfig == null ? ProgressConfig.callbackDefault() : progressConfig);
        if (header == null)
            header = new HashMap<>();

        builder = new Request.Builder()
                .url(baseUrl)
                .headers(Headers.of(header))
                .put(requestBody);

        return customizeOkHttpClient(httpClient).newCall(createRequest());
    }
}
