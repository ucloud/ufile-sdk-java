package cn.ucloud.ufile.api.object.multi;

import cn.ucloud.ufile.api.object.UfileObjectApi;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.auth.UfileAuthorizationException;
import cn.ucloud.ufile.auth.sign.UfileSignatureException;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.PostJsonRequestBuilder;
import cn.ucloud.ufile.util.HttpMethod;
import com.google.gson.JsonElement;
import okhttp3.MediaType;
import okhttp3.Response;

import java.util.Date;

/**
 * API-初始化分片上传
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:08
 */
public class InitMultiUploadApi extends UfileObjectApi<MultiUploadInfo> {
    /**
     * Required
     * 上传云端后的文件名
     */
    protected String keyName;

    /**
     * Required
     * 上传对象的mimeType
     */
    protected String mimeType;

    /**
     * Required
     * 根据MimeType解析成okhttp可用的mediaType，解析失败则代表mimeType无效
     */
    protected MediaType mediaType;


    /**
     * Required
     * 要上传的目标Bucket
     */
    protected String bucketName;

    /**
     * 构造方法
     *
     * @param authorizer Object授权器
     * @param host       API域名
     * @param httpClient Http客户端
     */
    public InitMultiUploadApi(ObjectAuthorizer authorizer, String host, HttpClient httpClient) {
        super(authorizer, host, httpClient);
    }

    /**
     * 配置分片上传到云端的对象名称
     *
     * @param keyName 对象名称
     * @return {@link InitMultiUploadApi}
     */
    public InitMultiUploadApi nameAs(String keyName) {
        this.keyName = keyName;
        return this;
    }

    /**
     * 配置分片上传到云端的对象的MIME类型
     *
     * @param mimeType MIME类型
     * @return {@link InitMultiUploadApi}
     */
    public InitMultiUploadApi withMimeType(String mimeType) {
        this.mimeType = mimeType;
        this.mediaType = MediaType.parse(mimeType);
        return this;
    }

    /**
     * 配置分片上传到的Bucket
     *
     * @param bucketName bucket名称
     * @return {@link InitMultiUploadApi}
     */
    public InitMultiUploadApi toBucket(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public InitMultiUploadApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileParamException, UfileAuthorizationException, UfileSignatureException {
        parameterValidat();

        String contentType = mediaType.toString();
        String date = dateFormat.format(new Date(System.currentTimeMillis()));
        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.POST, bucketName, keyName,
                contentType, "", date).setOptional(authOptionalData));

        PostJsonRequestBuilder builder = new PostJsonRequestBuilder();
        call = builder.baseUrl(generateFinalHost(bucketName, keyName) + "?uploads")
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

        if (mimeType == null || mimeType.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'mimeType' can not be null or empty");

        if (mediaType == null)
            throw new UfileParamException(
                    "The required param 'mimeType' is invalid");

        if (bucketName == null || bucketName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'bucketName' can not be null or empty");
    }

    @Override
    public MultiUploadInfo parseHttpResponse(Response response) throws UfileServerException, UfileClientException {
        MultiUploadInfo state = super.parseHttpResponse(response);
        state.setMimeType(mimeType);
        return state;
    }
}
