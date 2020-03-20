package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.bean.ObjectProfile;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.HeadRequestBuilder;
import cn.ucloud.ufile.util.HttpMethod;
import cn.ucloud.ufile.util.JLog;
import cn.ucloud.ufile.util.Parameter;
import com.google.gson.JsonElement;
import okhttp3.Response;

import java.util.*;

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
     * @param authorizer   Object授权器
     * @param objectConfig ObjectConfig {@link ObjectConfig}
     * @param httpClient   Http客户端
     */
    protected ObjectProfileApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
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
    protected void prepareData() throws UfileClientException {
        parameterValidat();

        contentType = "application/json; charset=utf-8";
        String date = dateFormat.format(new Date(System.currentTimeMillis()));
        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.HEAD, bucketName, keyName,
                contentType, "", date).setOptional(authOptionalData));

        call = new HeadRequestBuilder()
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
    public ObjectProfile parseHttpResponse(Response response) throws UfileServerException {
        ObjectProfile result = new ObjectProfile();
        int code = response.code();

        if (code == RESP_CODE_SUCCESS) {
            result.setContentLength(Long.parseLong(response.header("Content-Length", "0")));
            result.setContentType(response.header("Content-Type", ""));
            result.seteTag(response.header("ETag", "").replace("\"", ""));
            result.setAcceptRanges(response.header("Accept-Ranges", ""));
            result.setCreateTime(response.header("X-Ufile-Create-Time", ""));
            result.setLastModified(response.header("Last-Modified", ""));
            result.setStorageType(response.header("X-Ufile-Storage-Class", ""));
            result.setRestoreTime(response.header("X-Ufile-Restore", ""));

            if (response.headers() != null) {
                Set<String> names = response.headers().names();
                if (names != null) {
                    Map<String, String> headers = new HashMap<>();
                    Map<String, String> metadata = new HashMap<>();
                    for (String name : names) {
                        headers.put(name, response.header(name, null));
                        if (name == null || !name.startsWith("X-Ufile-Meta-"))
                            continue;

                        String key = name.substring(13).toLowerCase();
                        metadata.put(key, response.header(name, ""));
                    }
                    result.setHeaders(headers);
                    result.setMetadatas(metadata);
                }
            }
            result.setBucket(bucketName);
            result.setKeyName(keyName);

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
