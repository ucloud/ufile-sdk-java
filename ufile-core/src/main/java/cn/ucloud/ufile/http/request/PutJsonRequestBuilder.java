package cn.ucloud.ufile.http.request;

import com.google.gson.JsonObject;
import okhttp3.*;

import java.util.HashMap;

/**
 * Http PUT<Json> 请求构造器
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/9 11:55
 */
public class PutJsonRequestBuilder extends HttpRequestBuilder<JsonObject> {

    @Override
    public Call build(OkHttpClient httpClient) {
        if (params == null)
            params = new JsonObject();

        RequestBody requestBody = RequestBody.create(mediaType, params.toString());
        builder = new Request.Builder().put(requestBody);

        builder.url(baseUrl);

        if (header == null)
            header = new HashMap<>();

        builder.headers(Headers.of(header));

        return customizeOkHttpClient(httpClient).newCall(createRequest());
    }
}
