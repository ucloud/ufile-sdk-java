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
import java.util.List;
import java.util.HashMap;
import java.util.Map;


/**
 * API-生成私有上传URL（预签名PUT URL）
 *
 * @author: UCloud
 * @date: 2025/11/11
 */
public class GenerateObjectPrivateUploadUrlApi {
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
     * 私有上传路径的有效时间（单位：秒）
     * 即：生成的上传URL会在 创建时刻的时间戳 + expiresDuration秒 后的时刻过期
     */
    private long expiresDuration;

    /**
     * Optional
     * 上传文件的Content-Type
     */
    private String contentType;

    /**
     * Optional
     * 用户可选签名参数
     */
    private JsonElement authOptionalData;

    /**
     * Optional
     * STS 临时授权 SecurityToken
     */
    private String securityToken;

    /**
     * Optional
     * 文件存储类型：STANDARD（标准）| IA（低频）| ARCHIVE（归档）
     */
    private String storageType;

    /**
     * Optional
     * 用户自定义元数据（metadata）
     */
    private Map<String, String> metadatas;

    /**
     * Optional
     * 图片处理服务
     */
    private String iopCmd;

    /**
     * 构造方法
     *
     * @param authorizer      Object授权器
     * @param objectConfig    ObjectConfig {@link ObjectConfig}
     * @param keyName         对象名称
     * @param bucketName      bucket名称
     * @param expiresDuration 私有上传路径的有效时间（单位：秒）
     */
    protected GenerateObjectPrivateUploadUrlApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig,
                                                String keyName, String bucketName, int expiresDuration) {
        this.authorizer = authorizer;
        this.objectConfig = objectConfig;
        this.keyName = keyName;
        this.bucketName = bucketName;
        this.expiresDuration = expiresDuration;
    }

    /**
     * 设置上传文件的Content-Type
     *
     * @param contentType 文件MIME类型，例如：image/png, application/pdf
     * @return {@link GenerateObjectPrivateUploadUrlApi}
     */
    public GenerateObjectPrivateUploadUrlApi withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 设置文件存储类型
     *
     * @param storageType 存储类型：STANDARD（标准）| IA（低频）| ARCHIVE（归档）
     * @return {@link GenerateObjectPrivateUploadUrlApi}
     */
    public GenerateObjectPrivateUploadUrlApi withStorageType(String storageType) {
        this.storageType = storageType;
        return this;
    }

    /**
     * 添加自定义元数据
     *
     * @param key   元数据键
     * @param value 元数据值
     * @return {@link GenerateObjectPrivateUploadUrlApi}
     */
    public GenerateObjectPrivateUploadUrlApi addMetadata(String key, String value) {
        if (this.metadatas == null) {
            this.metadatas = new HashMap<>();
        }
        this.metadatas.put(key, value);
        return this;
    }

    /**
     * 设置所有元数据（会覆盖之前的设置）
     *
     * @param metadatas 元数据映射
     * @return {@link GenerateObjectPrivateUploadUrlApi}
     */
    public GenerateObjectPrivateUploadUrlApi withMetadatas(Map<String, String> metadatas) {
        this.metadatas = metadatas;
        return this;
    }

    /**
     * 针对图片文件的操作参数
     *
     * @param iopCmd 请参考 https://docs.ucloud.cn/ufile/service/pic
     * @return {@link GenerateObjectPrivateUploadUrlApi}
     */
    public GenerateObjectPrivateUploadUrlApi withIopCmd(String iopCmd) {
        this.iopCmd = iopCmd;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return {@link GenerateObjectPrivateUploadUrlApi}
     */
    public GenerateObjectPrivateUploadUrlApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }
    
    /**
     * 使用STS临时凭证签名
     *
     * @param securityToken STS token
     * @return {@link GenerateObjectPrivateUploadUrlApi}
     */
    public GenerateObjectPrivateUploadUrlApi withSecurityToken(String securityToken) {
        this.securityToken = securityToken;
        return this;
    }

    /**
     * 生成上传URL
     *
     * @return 预签名的上传URL
     * @throws UfileRequiredParamNotFoundException 必要参数未找到时抛出
     * @throws UfileAuthorizationException         授权异常时抛出
     * @throws UfileSignatureException             签名异常时抛出
     */
    public String createUrl() throws UfileClientException {
        parameterValidat();
        long expiresTime = System.currentTimeMillis() / 1000 + expiresDuration;

        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }
        String signature = authorizer.authorizePrivateUrl(
                (ObjectDownloadAuthParam) new ObjectDownloadAuthParam(HttpMethod.PUT, bucketName, keyName, expiresTime)
                        .setContentType(contentType)
                        .setOptional(authOptionalData));


        GetRequestBuilder builder = (GetRequestBuilder) new GetRequestBuilder()
                .baseUrl(generateFinalHost(bucketName, keyName));

        builder.addParam(new Parameter("UCloudPublicKey", authorizer.getPublicKey()))
                .addParam(new Parameter("Signature", signature))
                .addParam(new Parameter("Expires", String.valueOf(expiresTime)));

        // 添加 STS 安全令牌
        if (securityToken != null && !securityToken.isEmpty()) {
            builder.addParam(new Parameter("SecurityToken", securityToken));
        }

        String url = builder.generateGetUrl(builder.getBaseUrl(), builder.getParams());
        
        // 添加图片处理参数
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

    /**
     * 生成上传URL的同时返回客户端在上传时应该使用的HTTP头信息
     *
     * @return UploadUrlInfo 包含URL和建议HTTP头的信息对象
     * @throws UfileClientException 客户端异常时抛出
     */
    public UploadUrlInfo createUrlWithHeaders() throws UfileClientException {
        String url = createUrl();
        Map<String, String> headers = new HashMap<>();
        
        // Content-Type：上传请求头必须与签名时一致
        if (contentType != null && !contentType.isEmpty()) {
            headers.put("Content-Type", contentType);
        }
        
        return new UploadUrlInfo(url, headers);
    }

    /**
     * 异步生成上传URL
     *
     * @param callback 异步生成结果回调
     */
    public void createUrlAsync(final CreatePrivateUploadUrlCallback callback) {
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

    /**
     * 异步生成上传URL及HTTP头
     *
     * @param callback 异步生成结果回调
     */
    public void createUrlWithHeadersAsync(final CreatePrivateUploadUrlWithHeadersCallback callback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    UploadUrlInfo info = createUrlWithHeaders();
                    if (callback != null)
                        callback.onSuccess(info);
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

        if (objectConfig.isCustomDomain()) {
            try {
                // Encode keyName for URL path, but keep '/' separators.
                String encodedKeyName = Encoder.urlEncodePath(keyName, "UTF-8");
                return String.format("%s/%s", objectConfig.getCustomHost(), encodedKeyName);
            } catch (UnsupportedEncodingException e) {
                throw new UfileClientException("Occur error during URLEncode keyName", e);
            }
        }

        try {
            bucketName = Encoder.urlEncode(bucketName, "UTF-8");
            String region = Encoder.urlEncode(objectConfig.getRegion(), "UTF-8");
            String proxySuffix = Encoder.urlEncode(objectConfig.getProxySuffix(), "UTF-8");
            // Encode keyName for URL path, but keep '/' separators (critical for presigned URL signature).
            keyName = Encoder.urlEncodePath(keyName, "UTF-8");
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

    /**
     * 上传URL信息类
     * 包含预签名URL和建议使用的HTTP请求头
     */
    public static class UploadUrlInfo {
        private String url;
        private Map<String, String> headers;

        public UploadUrlInfo(String url, Map<String, String> headers) {
            this.url = url;
            this.headers = headers;
        }

        public String getUrl() {
            return url;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        @Override
        public String toString() {
            return "UploadUrlInfo{" +
                    "url='" + url + '\'' +
                    ", headers=" + headers +
                    '}';
        }
    }

    /**
     * 异步生成URL回调接口
     */
    public interface CreatePrivateUploadUrlCallback {
        void onSuccess(String url);
        void onFailed(UfileClientException e);
    }

    /**
     * 异步生成URL及HTTP头回调接口
     */
    public interface CreatePrivateUploadUrlWithHeadersCallback {
        void onSuccess(UploadUrlInfo info);
        void onFailed(UfileClientException e);
    }
}







