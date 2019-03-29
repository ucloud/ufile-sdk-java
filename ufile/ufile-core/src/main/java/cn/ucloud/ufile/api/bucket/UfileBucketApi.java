package cn.ucloud.ufile.api.bucket;

import cn.ucloud.ufile.annotation.UcloudParam;
import cn.ucloud.ufile.auth.BucketAuthorizer;
import cn.ucloud.ufile.api.UfileApi;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.GetRequestBuilder;
import cn.ucloud.ufile.util.Parameter;
import cn.ucloud.ufile.util.ParameterMaker;
import okhttp3.MediaType;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Ufile Bucket相关API基类
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/13 11:21
 */
public abstract class UfileBucketApi<T> extends UfileApi<T> {
    protected final String TAG = getClass().getSimpleName();

    /**
     * UCloud Ufile Bucket 域名
     */
    protected static final String UFILE_BUCKET_API_HOST = "http://api.ucloud.cn";

    /**
     * Bucket API 请求动作描述
     */
    @UcloudParam("Action")
    protected String action;
    /**
     * Bucket API授权器
     */
    protected BucketAuthorizer authorizer;

    /**
     * 构造方法
     *
     * @param authorizer Bucket授权器
     * @param httpClient Http客户端
     * @param action     API 请求动作描述
     */
    protected UfileBucketApi(BucketAuthorizer authorizer, HttpClient httpClient, String action) {
        super(httpClient, UFILE_BUCKET_API_HOST);
        this.authorizer = authorizer;
        this.action = action;
    }

    @Override
    protected void prepareData() throws UfileClientException {
        try {
            parameterValidat();

            List<Parameter<String>> query = ParameterMaker.makeParameter(this);
            query.add(new Parameter("PublicKey", authorizer.getPublicKey()));
            String signature = authorizer.authorizeBucketUrl(query);
            query.add(new Parameter<>("Signature", signature));

            call = new GetRequestBuilder()
                    .setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut)
                    .baseUrl(host)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("Accpet", "*/*")
                    .params(query)
                    .mediaType(MediaType.parse("application/json; charset=utf-8"))
                    .build(httpClient.getOkHttpClient());
        } catch (IllegalAccessException e) {
            throw new UfileClientException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new UfileClientException(e.getMessage(), e);
        }
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (action == null || action.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'action' can not be null or empty");
    }
}
