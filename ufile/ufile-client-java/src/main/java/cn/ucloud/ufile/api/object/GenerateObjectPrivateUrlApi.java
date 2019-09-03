package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectDownloadAuthParam;
import cn.ucloud.ufile.auth.UfileAuthorizationException;
import cn.ucloud.ufile.auth.sign.UfileSignatureException;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.http.request.GetRequestBuilder;
import cn.ucloud.ufile.util.HttpMethod;
import cn.ucloud.ufile.util.Parameter;
import com.google.gson.JsonElement;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * API-生成私有下载URL
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/16 12:00
 */
public class GenerateObjectPrivateUrlApi {
    private ObjectAuthorizer authorizer;
    private String host;

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
     * Required
     * 私有下载路径的有效时间，即：生成的下载URL会在 创建时刻的时间戳 + expiresDuration毫秒 后的时刻过期
     */
    private long expiresDuration;

    /**
     * 用户可选签名参数
     */
    private JsonElement authOptionalData;

    /**
     * 构造方法
     *
     * @param authorizer      Object授权器
     * @param host            API域名
     * @param keyName         对象名称
     * @param bucketName      bucket名称
     * @param expiresDuration 私有下载路径的有效时间，即：生成的下载URL会在 创建时刻的时间戳 + expiresDuration毫秒 后的时刻过期
     */
    protected GenerateObjectPrivateUrlApi(ObjectAuthorizer authorizer, String host, String keyName, String bucketName, int expiresDuration) {
        this.authorizer = authorizer;
        this.host = host;
        this.keyName = keyName;
        this.bucketName = bucketName;
        this.expiresDuration = expiresDuration;
    }


    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public GenerateObjectPrivateUrlApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    /**
     * 生成下载URL
     *
     * @return 下载URL
     * @throws UfileRequiredParamNotFoundException 必要参数未找到时抛出
     * @throws UfileAuthorizationException         授权异常时抛出
     * @throws UfileSignatureException             签名异常时抛出
     */
    public String createUrl() throws UfileClientException {
        parameterValidat();
        long expiresTime = System.currentTimeMillis() / 1000 + expiresDuration;

        String signature = authorizer.authorizePrivateUrl(
                (ObjectDownloadAuthParam) new ObjectDownloadAuthParam(HttpMethod.GET, bucketName, keyName, expiresTime)
                        .setOptional(authOptionalData));

        GetRequestBuilder builder = (GetRequestBuilder) new GetRequestBuilder()
                .baseUrl(generateFinalHost(bucketName, keyName));

        return builder.addParam(new Parameter("UCloudPublicKey", authorizer.getPublicKey()))
                .addParam(new Parameter("Signature", signature))
                .addParam(new Parameter("Expires", String.valueOf(expiresTime)))
                .generateGetUrl(builder.getBaseUrl(), builder.getParams());
    }

    public interface CreatePrivateUrlCallback {
        void onSuccess(String url);

        void onFailed(UfileClientException e);
    }

    /**
     * 生成下载URL(异步)
     *
     * @param callback 异步生成结果回调
     * @return 下载URL
     * @throws UfileRequiredParamNotFoundException 必要参数未找到时抛出
     * @throws UfileAuthorizationException         授权异常时抛出
     * @throws UfileSignatureException             签名异常时抛出
     */
    public void createUrlAsync(final CreatePrivateUrlCallback callback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    parameterValidat();
                    long expiresTime = System.currentTimeMillis() / 1000 + expiresDuration;

                    String signature = authorizer.authorizePrivateUrl(
                            (ObjectDownloadAuthParam) new ObjectDownloadAuthParam(HttpMethod.GET, bucketName, keyName, expiresTime)
                                    .setOptional(authOptionalData));

                    GetRequestBuilder builder = (GetRequestBuilder) new GetRequestBuilder()
                            .baseUrl(generateFinalHost(bucketName, keyName));

                    String url = builder.addParam(new Parameter("UCloudPublicKey", authorizer.getPublicKey()))
                            .addParam(new Parameter("Signature", signature))
                            .addParam(new Parameter("Expires", String.valueOf(expiresTime)))
                            .generateGetUrl(builder.getBaseUrl(), builder.getParams());
                    if (callback != null)
                        callback.onSuccess(url);
                } catch (UfileClientException e) {
                    if (callback != null)
                        callback.onFailed(e);
                }
            }
        }.start();
    }

    private String generateFinalHost(String bucketName, String keyName) throws UfileClientException {
        if (host == null || host.length() == 0)
            return host;

        if (host.startsWith("http"))
            return String.format("%s/%s", host, keyName);

        try {
            bucketName = URLEncoder.encode(bucketName, "UTF-8").replace("+","%20");
            keyName = URLEncoder.encode(keyName, "UTF-8").replace("+","%20");
            return String.format("http://%s.%s/%s", bucketName, host, keyName);
        } catch (UnsupportedEncodingException e) {
            throw new UfileClientException("Occur error during URLEncode bucketName and keyName");
        }
    }

    protected void parameterValidat() throws UfileParamException {
        if (keyName == null || keyName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'keyName' can not be null or empty");

        if (bucketName == null || bucketName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'bucketName' can not be null or empty");

        if (expiresDuration <= 0)
            throw new UfileParamException(
                    "The required param 'expiresDuration' must > 0");
    }
}
