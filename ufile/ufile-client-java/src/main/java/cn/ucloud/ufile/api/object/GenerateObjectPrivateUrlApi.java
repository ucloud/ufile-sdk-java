package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectDownloadAuthParam;
import cn.ucloud.ufile.auth.UfileAuthorizationException;
import cn.ucloud.ufile.auth.sign.UfileSignatureException;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.http.request.GetRequestBuilder;
import cn.ucloud.ufile.util.Encoder;
import cn.ucloud.ufile.util.HttpMethod;
import cn.ucloud.ufile.util.Parameter;
import com.google.gson.JsonElement;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;


/**
 * API-生成私有下载URL
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/16 12:00
 */
public class GenerateObjectPrivateUrlApi {
    private ObjectAuthorizer authorizer;
    private ObjectConfig objectConfig;

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
     * Ufile 文件下载链接所需的Content-Disposition: attachment文件名
     */
    private String attachmentFileName;

    /**
     * 用户可选签名参数
     */
    private JsonElement authOptionalData;

    /**
     * 图片处理服务
     */
    protected String iopCmd;

    /**
     * 构造方法
     *
     * @param authorizer      Object授权器
     * @param objectConfig    ObjectConfig {@link ObjectConfig}
     * @param keyName         对象名称
     * @param bucketName      bucket名称
     * @param expiresDuration 私有下载路径的有效时间，即：生成的下载URL会在 创建时刻的时间戳 + expiresDuration毫秒 后的时刻过期
     */
    protected GenerateObjectPrivateUrlApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig,
                                          String keyName, String bucketName, int expiresDuration) {
        this.authorizer = authorizer;
        this.objectConfig = objectConfig;
        this.keyName = keyName;
        this.bucketName = bucketName;
        this.expiresDuration = expiresDuration;
    }

    /**
     * 使用Content-Disposition: attachment，并配置attachment的文件名
     *
     * @param attachmentFileName Content-Disposition: attachment的文件名
     * @return
     */
    public GenerateObjectPrivateUrlApi withAttachment(String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
        return this;
    }

    /**
     * 使用Content-Disposition: attachment，并且文件名默认为keyName
     *
     * @return
     */
    public GenerateObjectPrivateUrlApi withAttachment() {
        this.attachmentFileName = keyName;
        return this;
    }

    /**
     * 针对图片文件的操作参数
     *
     * @param iopCmd 请参考 https://docs.ucloud.cn/ufile/service/pic
     * @return {@link GenerateObjectPrivateUrlApi}
     */
    public GenerateObjectPrivateUrlApi withIopCmd(String iopCmd) {
        this.iopCmd = iopCmd;
        return this;
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

        builder.addParam(new Parameter("UCloudPublicKey", authorizer.getPublicKey()))
                .addParam(new Parameter("Signature", signature))
                .addParam(new Parameter("Expires", String.valueOf(expiresTime)));

        if (attachmentFileName != null && !attachmentFileName.isEmpty()) {
            try {
                attachmentFileName = Encoder.urlEncode(attachmentFileName, "UTF-8");
                builder.addParam(new Parameter("ufileattname", attachmentFileName));
            } catch (UnsupportedEncodingException e) {
                throw new UfileClientException("Occur error during URLEncode attachmentFileName", e);
            }
        }


        String url = builder.generateGetUrl(builder.getBaseUrl(), builder.getParams());
        if (iopCmd != null && !iopCmd.isEmpty()) {
            List<Parameter<String>> params = builder.getParams();
            if (params == null || params.isEmpty()) {
                url = String.format("%s?%s", url, iopCmd);
            } else {
                url = String.format("%s&%s", url, iopCmd);
            }
        }

        return url;
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
                    String url = createUrl();
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
        if (objectConfig == null)
            return null;

        if (objectConfig.isCustomDomain())
            return String.format("%s/%s", objectConfig.getCustomHost(), keyName);

        try {
            bucketName = Encoder.urlEncode(bucketName, "UTF-8");
            String region = Encoder.urlEncode(objectConfig.getRegion(), "UTF-8");
            String proxySuffix = Encoder.urlEncode(objectConfig.getProxySuffix(), "UTF-8");
            keyName = Encoder.urlEncode(keyName, "UTF-8");
            return new StringBuilder(objectConfig.getProtocol().getValue())
                    .append(String.format("%s.%s.%s/%s", bucketName, region, proxySuffix, keyName)).toString();
        } catch (UnsupportedEncodingException e) {
            throw new UfileClientException("Occur error during URLEncode bucketName and keyName", e);
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
