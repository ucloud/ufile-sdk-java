package cn.ucloud.ufile.http;

import cn.ucloud.ufile.api.ApiError;
import okhttp3.Request;

/**
 * Http回调基类 <R, E> = <ResponseBean, ErrorBean>
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/13 10:56
 */
public abstract class BaseHttpCallback<R, E> implements OnProgressListener {
    protected String TAG = getClass().getSimpleName();

    @Override
    public void onProgress(long bytesWritten, long contentLength) {

    }

    /**
     * Response回调
     *
     * @param response response，泛型指定
     */
    public abstract void onResponse(R response);

    /**
     * Error回调
     *
     * @param request  请求体，部分情况的Error会返回null
     * @param error    错误类,{@link ApiError}，onError的主要判断依据
     * @param response response，泛型指定，部分情况无法正常解析出指定的Response，会返回null
     */
    public abstract void onError(Request request, ApiError error, E response);
}
