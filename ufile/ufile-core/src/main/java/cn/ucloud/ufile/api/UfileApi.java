package cn.ucloud.ufile.api;

import cn.ucloud.ufile.exception.UfileIOException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileServerException;
import com.google.gson.Gson;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.http.UfileHttpException;
import cn.ucloud.ufile.http.BaseHttpCallback;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.response.ResponseParser;
import com.google.gson.JsonElement;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;

/**
 * Ufile API请求基类
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 20:05
 */
public abstract class UfileApi<T> implements Callback, ResponseParser<T, UfileErrorBean> {
    protected final String TAG = getClass().getSimpleName();
    /**
     * API的地址
     */
    protected String host;
    /**
     * Http客户端
     */
    protected HttpClient httpClient;
    /**
     * Http请求
     */
    protected Call call;
    /**
     * Http API回调
     */
    protected BaseHttpCallback<T, UfileErrorBean> httpCallback;
    /**
     * Ufile Http API指定的Date格式
     */
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * 成功时的Http response code
     */
    protected int RESP_CODE_SUCCESS = 200;


    /**
     * 用户可选签名参数
     */
    protected JsonElement authOptionalData;


    /**
     * 构造方法
     *
     * @param httpClient httpClient
     * @param host       API 域名
     */
    protected UfileApi(HttpClient httpClient, String host) {
        this.httpClient = httpClient;
        this.host = host;
    }

    /**
     * API请求前数据准备
     *
     * @throws UfileClientException Ufile业务异常
     */
    protected abstract void prepareData() throws UfileClientException;

    protected abstract void parameterValidat() throws UfileParamException;

    /**
     * 执行API - 同步(阻塞)
     *
     * @return 泛型的Response返回值
     * @throws UfileClientException Ufile业务异常
     */
    public T execute() throws UfileClientException, UfileServerException {
        prepareData();

        try {
            Response response = call.execute();
            if (response == null)
                throw new UfileHttpException("Response is null");

            if (response.code() != RESP_CODE_SUCCESS)
                throw new UfileServerException(parseErrorResponse(response));

            return parseHttpResponse(response);
        } catch (IOException e) {
            throw new UfileIOException("Occur IOException while sending http request");
        }
    }

    /**
     * 执行API - 异步
     *
     * @param callback API异步执行回调
     */
    public void executeAsync(BaseHttpCallback<T, UfileErrorBean> callback) {
        httpCallback = callback;

        new Thread() {
            @Override
            public void run() {
                try {
                    prepareData();
                    call.enqueue(UfileApi.this);
                } catch (UfileClientException e) {
                    if (callback != null)
                        httpCallback.onError(null, new ApiError(ApiError.ErrorType.ERROR_PARAMS_ILLEGAL, e), null);
                }
            }
        }.start();
    }

    @Override
    public void onFailure(Call call, IOException e) {
        if (httpCallback != null)
            httpCallback.onError(call.request(), new ApiError(ApiError.ErrorType.ERROR_NETWORK_ERROR, e), null);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response == null) {
            if (httpCallback != null)
                httpCallback.onError(call.request(), new ApiError(ApiError.ErrorType.ERROR_RESPONSE_IS_NULL), null);
            return;
        }

        if (response.code() != RESP_CODE_SUCCESS) {
            if (httpCallback != null) {
                UfileErrorBean e = null;
                try {
                    e = parseErrorResponse(response);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                } finally {
                    httpCallback.onError(call.request(),
                            new ApiError(ApiError.ErrorType.ERROR_SERVER_ERROR, "Response-Code = " + response.code())
                                    .setResponseCode(response.code()), e);
                }
            }
            return;
        }

        try {
            T res = parseHttpResponse(response);
            if (res == null) {
                if (httpCallback != null)
                    httpCallback.onError(call.request(),
                            new ApiError(ApiError.ErrorType.ERROR_RESPONSE_SPARSE_FAILED, "The result of parseHttpResponse is null")
                                    .setResponseCode(response.code()), null);

                return;
            }

            if (httpCallback != null)
                httpCallback.onResponse(res);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            if (httpCallback != null)
                httpCallback.onError(call.request(),
                        new ApiError(ApiError.ErrorType.ERROR_RESPONSE_SPARSE_FAILED, throwable)
                                .setResponseCode(response.code()), null);
        }
    }

    /**
     * 解析请求成功后的Http response
     *
     * @param response 源Http response
     * @return 指定的泛型Response Bean
     * @throws Exception 异常
     */
    @Override
    public T parseHttpResponse(Response response) throws UfileClientException, UfileServerException {
        try {
            Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            String content = response.body().string();
            response.body().close();
            content = (content == null || content.length() == 0) ? "{}" : content;
            return new Gson().fromJson(content, type);
        } catch (IOException e) {
            throw new UfileIOException("Occur IOException while parsing response data");
        }
    }

    /**
     * 解析请求失败后的异常信息
     *
     * @param response 源Http response
     * @return 异常数据Bean {@link UfileErrorBean}
     * @throws Exception 异常
     */
    @Override
    public UfileErrorBean parseErrorResponse(Response response) throws UfileClientException {
        try {
            String content = response.body().string();
            response.body().close();
            content = (content == null || content.length() == 0) ? "{}" : content;
            UfileErrorBean errorBean = new Gson().fromJson(content, UfileErrorBean.class);
            errorBean.setResponseCode(response.code());
            errorBean.setxSessionId(response.header("X-SessionId"));
            return errorBean;
        } catch (IOException e) {
            throw new UfileIOException("Occur IOException while parsing error data");
        }
    }
}
