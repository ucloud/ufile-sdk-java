package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.auth.UfileAuthorizationException;
import cn.ucloud.ufile.auth.sign.UfileSignatureException;
import cn.ucloud.ufile.bean.ObjectRestoreBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.HeadRequestBuilder;
import cn.ucloud.ufile.http.request.HttpRequestBuilder;
import cn.ucloud.ufile.http.request.PutFileRequestBuilder;
import cn.ucloud.ufile.http.request.PutJsonRequestBuilder;
import cn.ucloud.ufile.util.HttpMethod;
import com.google.gson.JsonElement;
import okhttp3.Response;

import java.util.Date;

/**
 * API-获取云端对象描述信息
 *
 * @author: delex
 * @E-mail: delex.xie@ucloud.cn
 * @date: 2019/08/27 19:09
 */
public class ObjectRestoreApi extends UfileObjectApi<ObjectRestoreBean> {
    /**
     * Required
     * 云端对象名称
     */
    private String keyName;
    /**
     * Required
     * Bucket空间名称
     */
    private String bucketName;

    /**
     * 构造方法
     *
     * @param authorizer Object授权器
     * @param host       API域名
     * @param httpClient Http客户端
     */
    protected ObjectRestoreApi(ObjectAuthorizer authorizer, String host, HttpClient httpClient) {
        super(authorizer, host, httpClient);
    }

    /**
     * 配置需要获取信息的云端对象名称
     *
     * @param keyName 对象名称
     * @return {@link ObjectRestoreApi}
     */
    public ObjectRestoreApi which(String keyName) {
        this.keyName = keyName;
        return this;
    }

    /**
     * 配置需要获取信息的对象所在的Bucket
     *
     * @param bucketName bucket名称
     * @return {@link ObjectRestoreApi}
     */
    public ObjectRestoreApi atBucket(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public ObjectRestoreApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileParamException, UfileAuthorizationException, UfileSignatureException {
        parameterValidat();

        String keyName_tmp = keyName; //参数参与签名
        String date = dateFormat.format(new Date(System.currentTimeMillis()));
        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.PUT, bucketName, keyName_tmp,
                "", "", date).setOptional(authOptionalData));

        call = new PutJsonRequestBuilder()
                .setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut)
                .baseUrl(generateFinalHost(bucketName, keyName_tmp + "?restore"))
                .addHeader("Accpet", "*/*")
                .addHeader("Date", date)
                .addHeader("authorization", authorization)
                .build(httpClient.getOkHttpClient());
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (keyName == null || keyName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'keyName' can not be null or empty");

        if (bucketName == null || bucketName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'bucketName' can not be null or empty");
    }

    @Override
    public ObjectRestoreBean parseHttpResponse(Response response) throws UfileServerException {
        ObjectRestoreBean result = new ObjectRestoreBean();
        int code = response.code();

        if (code == RESP_CODE_SUCCESS) {
            return result;
        } else {
            throw new UfileServerException(parseErrorResponse(response));
        }
    }

    @Override
    public UfileErrorBean parseErrorResponse(Response response) {
        UfileErrorBean errorBean = new UfileErrorBean();
        errorBean.setxSessionId(response.header("X-SessionId"));
        int code = response.code();
        errorBean.setResponseCode(code);
        switch (code) {
            case 404: {
                errorBean.setErrMsg(String.format("The object '%s' is not existed in the bucket '%s'", keyName, bucketName));
                break;
            }
            default: {
                errorBean.setErrMsg(String.format("Http Error: Response-Code is %d", code));
                break;
            }
        }

        return errorBean;
    }
}
