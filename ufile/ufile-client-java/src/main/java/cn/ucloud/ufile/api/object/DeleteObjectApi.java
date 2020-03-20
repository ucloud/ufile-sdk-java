package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.bean.base.BaseBucketResponseBean;
import cn.ucloud.ufile.bean.base.BaseObjectResponseBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.DeleteRequestBuilder;
import cn.ucloud.ufile.util.HttpMethod;
import com.google.gson.JsonElement;
import okhttp3.Response;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * API-删除云端对象
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:08
 */
public class DeleteObjectApi extends UfileObjectApi<BaseObjectResponseBean> {
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
     * @param authorizer   Object授权器
     * @param objectConfig ObjectConfig {@link ObjectConfig}
     * @param httpClient   Http客户端
     */
    protected DeleteObjectApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
        RESP_CODE_SUCCESS = 204;
    }

    /**
     * 配置要删除的云端对象名称
     *
     * @param keyName 对象名称
     * @return {@link DeleteObjectApi}
     */
    public DeleteObjectApi keyName(String keyName) {
        this.keyName = keyName;
        return this;
    }

    /**
     * 配置要删除的对象所在Bucket
     *
     * @param bucketName bucket名称
     * @return {@link DeleteObjectApi}
     */
    public DeleteObjectApi atBucket(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public DeleteObjectApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileClientException {
        parameterValidat();
        contentType = "application/json; charset=utf-8";
        String date = dateFormat.format(new Date(System.currentTimeMillis()));

        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.DELETE, bucketName, keyName,
                contentType, "", date).setOptional(authOptionalData));

        call = new DeleteRequestBuilder()
                .setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut)
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
    public BaseObjectResponseBean parseHttpResponse(Response response) throws UfileClientException, UfileServerException {
        BaseObjectResponseBean result= super.parseHttpResponse(response);

        if (response.headers() != null) {
            Set<String> names = response.headers().names();
            if (names != null) {
                Map<String, String> headers = new HashMap<>();
                for (String name : names) {
                    headers.put(name, response.header(name, null));
                }
                result.setHeaders(headers);
            }
        }

        return result;
    }
}
