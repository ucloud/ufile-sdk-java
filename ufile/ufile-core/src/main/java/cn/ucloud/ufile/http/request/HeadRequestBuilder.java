package cn.ucloud.ufile.http.request;

import cn.ucloud.ufile.util.Parameter;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Http HEAD 请求构造器
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/9 11:55
 */
public class HeadRequestBuilder extends HttpRequestBuilder<List<Parameter<String>>> {

    public HttpRequestBuilder<List<Parameter<String>>> addParam(Parameter param) {
        if (params == null)
            params = new ArrayList<>();

        params.add(param);
        return this;
    }

    @Override
    public Call build(OkHttpClient httpClient) {
        if (params == null)
            params = new ArrayList<>();

        builder = new Request.Builder().head();

        String url = generateGetUrl(baseUrl, params);
        builder.url(url);

        if (header == null)
            header = new HashMap<>();

        builder.headers(Headers.of(header));
        return customizeOkHttpClient(httpClient).newCall(createRequest());
    }
}
