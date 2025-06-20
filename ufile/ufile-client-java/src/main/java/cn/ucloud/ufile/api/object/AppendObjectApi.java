package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.UfileConstants;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.bean.AppendObjectResultBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.*;
import cn.ucloud.ufile.http.BaseHttpCallback;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.ProgressConfig;
import cn.ucloud.ufile.http.request.PutStreamRequestBuilder;
import cn.ucloud.ufile.util.*;
import com.google.gson.JsonElement;
import okhttp3.MediaType;
import okhttp3.Response;

import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * API-Put上传流
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2019/03/26 22:24
 */
public class AppendObjectApi extends UfileObjectApi<AppendObjectResultBean> {
    /**
     * Required
     * Appendable云端对象名称
     */
    protected String keyName;
    /**
     * Required
     * 要append的数据
     */
    protected byte[] appendData;
    /**
     * Required
     * 要append到云端已有对象的偏移量，必须>=0
     */
    protected long position;
    /**
     * Required
     * 要上传的流mimeType
     */
    protected String mimeType;
    /**
     * Required
     * 根据MimeType解析成okhttp可用的mediaType，解析失败则代表mimeType无效
     */
    protected MediaType mediaType;

    /**
     * Required
     * Bucket空间名称
     */
    protected String bucketName;
    /**
     * 是否需要上传MD5校验码
     */
    private boolean isVerifyMd5 = true;

    private ProgressConfig progressConfig;

    /**
     * 流写入的buffer大小，Default = 256 KB
     */
    private int bufferSize = UfileConstants.DEFAULT_BUFFER_SIZE;
    
    /**
     * 安全令牌（STS临时凭证）
     */
    private String securityToken;

    /**
     * 构造方法
     *
     * @param authorizer   Object授权器
     * @param objectConfig ObjectConfig {@link ObjectConfig}
     * @param httpClient   Http客户端
     */
    protected AppendObjectApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
        progressConfig = ProgressConfig.callbackDefault();
    }

    /**
     * 设置要append的数据和MIME类型
     *
     * @param appendData 要append的数据
     * @param mimeType   MIME类型
     * @return {@link AppendObjectApi}
     */
    public AppendObjectApi from(byte[] appendData, String mimeType) {
        this.appendData = appendData;
        this.mimeType = mimeType;
        this.mediaType = MediaType.parse(mimeType);
        return this;
    }

    /**
     * 设置要上传到的Bucket名称
     *
     * @param bucketName bucket名称
     * @param keyName    Appendable云端对象名称
     * @param position   要append到云端已有对象的偏移量，值必须>=0
     * @return
     */
    public AppendObjectApi appendTo(String bucketName, String keyName, long position) {
        this.bucketName = bucketName;
        this.keyName = keyName;
        this.position = position;
        return this;
    }

    /**
     * 设置安全令牌（STS临时凭证）
     *
     * @param securityToken 安全令牌
     * @return {@link AppendObjectApi}
     */
    public AppendObjectApi withSecurityToken(String securityToken) {
        this.securityToken = securityToken;
        return this;
    }

    /**
     * 设置是否需要MD5校验
     *
     * @param isVerifyMd5 是否校验MD5
     * @return {@link AppendObjectApi}
     */
    public AppendObjectApi withVerifyMd5(boolean isVerifyMd5) {
        this.isVerifyMd5 = isVerifyMd5;
        return this;
    }

    /**
     * 配置进度回调设置
     *
     * @param config 进度回调设置
     * @return {@link AppendObjectApi}
     */
    public AppendObjectApi withProgressConfig(ProgressConfig config) {
        progressConfig = config == null ? this.progressConfig : config;
        return this;
    }

    /**
     * 设置流读写的Buffer大小，默认 256 KB
     *
     * @param bufferSize Buffer大小
     * @return {@link AppendObjectApi}
     */
    public AppendObjectApi setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public AppendObjectApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileClientException {
        parameterValidat();

        List<Parameter<String>> query = new ArrayList<>();
        query.add(new Parameter<>("position", String.valueOf(position)));

        contentType = mediaType.toString();
        String contentMD5 = "";
        String date = dateFormat.format(new Date(System.currentTimeMillis()));

        PutStreamRequestBuilder builder = new PutStreamRequestBuilder(onProgressListener);
        //keyName 进行了修改
        String keyName_tmp = keyName + "?append&" + builder.generateUrlQuery(query);
        builder.setBufferSize(bufferSize);
        builder.baseUrl(generateFinalHost(bucketName, keyName_tmp))
                .setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut)
                .header(headers)
                .addHeader("Content-Type", contentType)
                .addHeader("Accpet", "*/*")
                .addHeader("Date", date)
                .mediaType(mediaType);

        builder.addHeader("Content-Length", String.valueOf(appendData.length));

        if (isVerifyMd5) {
            try {
                contentMD5 = HexFormatter.formatByteArray2HexString(Encoder.md5(appendData), false);
                builder.addHeader("Content-MD5", contentMD5);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        if (securityToken != null && !securityToken.isEmpty()) {
            builder.addHeader("SecurityToken", securityToken);
        }

        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.PUT, bucketName, keyName_tmp,
                contentType, contentMD5, date).setOptional(authOptionalData));
        builder.addHeader("authorization", authorization);

        builder.params(new ByteArrayInputStream(appendData));
        builder.setProgressConfig(progressConfig);

        call = builder.build(httpClient.getOkHttpClient());
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (appendData == null)
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'appendData' can not be null");

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

        if (position < 0)
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'position' can not be below 0");
    }

    private OnProgressListener onProgressListener;

    /**
     * 配置进度监听器
     * 该配置可供execute()同步接口回调进度使用，若使用executeAsync({@link BaseHttpCallback})，则后配置的会覆盖新配置的
     *
     * @param onProgressListener 进度监听器
     * @return {@link AppendObjectApi}
     */
    public AppendObjectApi setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
        return this;
    }

    @Override
    public void executeAsync(BaseHttpCallback<AppendObjectResultBean, UfileErrorBean> callback) {
        onProgressListener = callback;
        super.executeAsync(callback);
    }

    @Override
    public AppendObjectResultBean parseHttpResponse(Response response) throws UfileClientException, UfileServerException {
        try {
            AppendObjectResultBean result = new AppendObjectResultBean();
            String eTag = response.header("ETag", null);
            eTag = eTag == null ? null : eTag.replace("\"", "");
            result.seteTag(eTag);

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
        } finally {
            FileUtil.close(response.body());
        }
    }
}
