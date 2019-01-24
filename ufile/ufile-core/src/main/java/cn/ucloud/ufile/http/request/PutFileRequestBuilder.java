package cn.ucloud.ufile.http.request;

import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.ProgressConfig;
import cn.ucloud.ufile.http.request.body.UploadFileRequestBody;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.File;
import java.util.HashMap;

/**
 * Http PUT<File> 请求构造器
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/9 11:55
 */
public class PutFileRequestBuilder extends HttpRequestBuilder<File> {
    private OnProgressListener onProgressListener;
    private ProgressConfig progressConfig;
    private UploadFileRequestBody requestBody;

    public PutFileRequestBuilder(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
        this.requestBody = new UploadFileRequestBody();
    }

    public PutFileRequestBuilder setProgressConfig(ProgressConfig progressConfig) {
        this.progressConfig = progressConfig;
        return this;
    }

    public PutFileRequestBuilder setBufferSize(long bufferSize) {
        this.requestBody.setBufferSize(bufferSize);
        return this;
    }

    @Override
    public Call build(OkHttpClient httpClient) {
        this.requestBody.setContent(params)
                .setContentType(mediaType)
                .setOnProgressListener(onProgressListener)
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
