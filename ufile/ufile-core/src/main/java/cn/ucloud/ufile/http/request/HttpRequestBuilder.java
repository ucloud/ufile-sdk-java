package cn.ucloud.ufile.http.request;

import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.util.Parameter;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Http 请求构造器基类
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/9 11:27
 */
public abstract class HttpRequestBuilder<T> {
    protected final String TAG = getClass().getSimpleName();

    /**
     * OkHttp 请求构造器
     */
    protected Request.Builder builder;
    /**
     * Http 请求域名
     */
    protected String baseUrl;
    /**
     * MIME类型
     */
    protected MediaType mediaType;
    /**
     * Tag
     */
    protected Object tag;
    /**
     * Request Header参数集
     */
    protected Map<String, String> header;
    /**
     * Request Body，泛型指定
     */
    protected T params;

    protected long readTimeOut;
    protected long writeTimeOut;
    protected long connTimeOut;

    public HttpRequestBuilder() {
    }

    public HttpRequestBuilder<T> tag(Object tag) {
        this.tag = tag;
        return this;
    }

    public HttpRequestBuilder<T> baseUrl(String url) {
        this.baseUrl = url;
        return this;
    }

    public HttpRequestBuilder<T> header(Map<String, String> header) {
        this.header = header;
        return this;
    }

    public HttpRequestBuilder<T> mediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public HttpRequestBuilder<T> params(T params) {
        this.params = params;
        return this;
    }

    public HttpRequestBuilder<T> addHeader(String key, String val) {
        if (header == null)
            header = new HashMap<>();

        header.put(key, val);
        return this;
    }

    public HttpRequestBuilder<T> setReadTimeOut(long readTimeOut) {
        this.readTimeOut = readTimeOut;
        return this;
    }

    public HttpRequestBuilder<T> setWriteTimeOut(long writeTimeOut) {
        this.writeTimeOut = writeTimeOut;
        return this;
    }

    public HttpRequestBuilder<T> setConnTimeOut(long connTimeOut) {
        this.connTimeOut = connTimeOut;
        return this;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public Object getTag() {
        return tag;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public T getParams() {
        return params;
    }

    /**
     * 生成URL参数Query,并对键值对做URLEncode
     *
     * @param query 参数键值对集合
     * @return URL参数Query (eg: a=1&b=B&c=true)
     */
    public String generateUrlQuery(List<Parameter<String>> query) {
        if (query == null || query.isEmpty())
            return "";

        StringBuffer queryBuffer = new StringBuffer();
        for (Parameter<String> param : query) {
            try {
                queryBuffer.append(URLEncoder.encode(param.key, "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(param.value, "UTF-8"))
                        .append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        String queryString = queryBuffer.deleteCharAt(queryBuffer.length() - 1).toString();
        if (queryString == null || queryString.length() == 0)
            return "";

        return queryBuffer.toString();
    }

    /**
     * 生成URL参数Query,并对键值对做URLEncode
     *
     * @param query 参数键值对集合
     * @return URL参数Query (eg: a=1&b=B&c=true)
     */
    public String generateUrlQuery(Map<String, String> query) {
        if (query == null || query.isEmpty())
            return "";

        StringBuffer queryBuffer = new StringBuffer();
        for (String key : query.keySet()) {
            try {
                queryBuffer.append(URLEncoder.encode(key, "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(query.get(key), "UTF-8"))
                        .append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        String queryString = queryBuffer.deleteCharAt(queryBuffer.length() - 1).toString();
        if (queryString == null || queryString.length() == 0)
            return "";

        return queryBuffer.toString();
    }


    /**
     * 生成带参数Query的URL,并对键值对做URLEncode
     *
     * @param url   URL
     * @param query 参数键值对集合
     * @return URL参数Query (eg: http://www.ucloud.com?a=1&b=B&c=true)
     */
    public String generateGetUrl(String url, Map<String, String> query) {
        String queryStr = generateUrlQuery(query);
        queryStr = (queryStr == null || queryStr.length() == 0) ? "" : ("?" + queryStr);
        return new StringBuffer(url).append(queryStr).toString();
    }

    /**
     * 生成带参数Query的URL,并对键值对做URLEncode
     *
     * @param url   URL
     * @param query 参数键值对集合
     * @return URL参数Query (eg: http://www.ucloud.com?a=1&b=B&c=true)
     */
    public String generateGetUrl(String url, List<Parameter<String>> query) {
        String queryStr = generateUrlQuery(query);
        queryStr = (queryStr == null || queryStr.length() == 0) ? "" : ("?" + queryStr);
        return new StringBuffer(url).append(queryStr).toString();
    }

    /**
     * 自定义OkHttpClient，因OkHttpClient重复创建造成性能和内存上的损失，但部分Http请求需要添加拦截器，来拦截请求做相关处理。
     * 而添加拦截器的操作只能对OkHttpClient操作，并且添加后无法移除，所以在此由原始OkHttpClient来复制Builder来创建临时Client
     *
     * @param httpClient 原始的OkHttpClient
     * @return
     */
    protected OkHttpClient customizeOkHttpClient(OkHttpClient httpClient) {
        if (httpClient == null)
            return null;

        if (readTimeOut > 0 || writeTimeOut > 0 || connTimeOut > 0) {
            readTimeOut = readTimeOut > 0 ? readTimeOut : HttpClient.DEFAULT_READ_TIMEOUT;
            writeTimeOut = writeTimeOut > 0 ? writeTimeOut : HttpClient.DEFAULT_WRITE_TIMEOUT;
            connTimeOut = connTimeOut > 0 ? connTimeOut : HttpClient.DEFAULT_CONNECT_TIMEOUT;

            return httpClient.newBuilder()
                    .readTimeout(readTimeOut, TimeUnit.MILLISECONDS)
                    .writeTimeout(writeTimeOut, TimeUnit.MILLISECONDS)
                    .connectTimeout(connTimeOut, TimeUnit.MILLISECONDS)
                    .build();
        }

        return httpClient;
    }

    /**
     * 创建请求
     *
     * @return OkHttp Request
     */
    protected Request createRequest() {
        if (builder != null)
            builder.removeHeader("User-Agent").addHeader("User-Agent", "Ufile SDK from UCloud Co.,Ltd.");

        return builder.build();
    }

    /**
     * 构建请求
     *
     * @param httpClient OkHttpClient
     * @return 请求
     */
    public abstract Call build(OkHttpClient httpClient);
}
