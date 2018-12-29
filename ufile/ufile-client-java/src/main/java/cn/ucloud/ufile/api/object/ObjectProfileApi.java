package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.auth.UfileAuthorizationException;
import cn.ucloud.ufile.auth.sign.UfileSignatureException;
import cn.ucloud.ufile.bean.ObjectProfile;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.UfileHttpException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.HeadRequestBuilder;
import cn.ucloud.ufile.util.HttpMethod;
import com.google.gson.JsonElement;
import okhttp3.Response;

import java.util.Date;

/**
 * API-获取云端对象描述信息
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:09
 */
public class ObjectProfileApi extends UfileObjectApi<ObjectProfile> {
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
    protected ObjectProfileApi(ObjectAuthorizer authorizer, String host, HttpClient httpClient) {
        super(authorizer, host, httpClient);
    }

    /**
     * 配置需要获取信息的云端对象名称
     *
     * @param keyName 对象名称
     * @return {@link ObjectProfileApi}
     */
    public ObjectProfileApi which(String keyName) {
        this.keyName = keyName;
        return this;
    }

    /**
     * 配置需要获取信息的对象所在的Bucket
     *
     * @param bucketName bucket名称
     * @return {@link ObjectProfileApi}
     */
    public ObjectProfileApi atBucket(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public ObjectProfileApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileParamException, UfileAuthorizationException, UfileSignatureException {
        parameterValidat();

        String contentType = "application/json; charset=utf-8";
        String date = dateFormat.format(new Date(System.currentTimeMillis()));
        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.HEAD, bucketName, keyName,
                contentType, "", date).setOptional(authOptionalData));

        call = new HeadRequestBuilder()
                .baseUrl(generateFinalHost(bucketName, keyName))
                .addHeader("Content-Type", contentType)
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
    public ObjectProfile parseHttpResponse(Response response) throws Exception {
        ObjectProfile result = new ObjectProfile();
        int code = response.code();

        if (code == RESP_CODE_SUCCESS) {
            result.setContentLength(Long.parseLong(response.header("Content-Length", "0")));
            result.setContentType(response.header("Content-Type", ""));
            result.seteTag(response.header("ETag", "").replace("\"", ""));
            result.setAcceptRanges(response.header("Accept-Ranges", ""));
            result.setLastModified(response.header("Last-Modified", ""));
            result.setBucket(bucketName);
            result.setKeyName(keyName);

            return result;
        } else {
            throw new UfileHttpException(parseErrorResponse(response).toString());
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
                errorBean.setErrMsg(String.format("Http error: Response-Code is %d", code));
                break;
            }
        }

        return errorBean;
    }
}
