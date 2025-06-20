package cn.ucloud.ufile.api.object.multi;

import cn.ucloud.ufile.UfileConstants;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.PutStreamApi;
import cn.ucloud.ufile.api.object.UfileObjectApi;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.exception.UfileServerException;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * API-上传分片数据
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:08
 */
public class MultiUploadPartApi extends UfileObjectApi<MultiUploadPartState> {
    /**
     * Required
     * 分片上传初始化状态
     */
    private MultiUploadInfo info;
    /**
     * Required
     * 需要上传的分片数据
     */
    private byte[] buffer;
    /**
     * buffer[] 偏移量
     */
    private int offset;
    /**
     * buffer[] 读取长度
     */
    private int length;
    /**
     * Required
     * 该分片的序号
     */
    private int partIndex;
    /**
     * 是否需要上传MD5校验码
     */
    private boolean isVerifyMd5 = true;

    /**
     * 流写入的buffer大小，Default = 256 KB
     */
    private int bufferSize = UfileConstants.DEFAULT_BUFFER_SIZE;

    /**
     * 进度回调配置
     */
    private ProgressConfig progressConfig;
    
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
    public MultiUploadPartApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
        progressConfig = ProgressConfig.callbackDefault();
    }

    /**
     * 配置需要上传的分片上传任务
     *
     * @param info 分片上传初始化信息，{@link MultiUploadInfo}
     * @return {@link MultiUploadPartApi}
     */
    public MultiUploadPartApi which(MultiUploadInfo info) {
        this.info = info;
        return this;
    }

    /**
     * 配置分片的数据和序号
     *
     * @param buffer    分片数据
     * @param partIndex 分片序号(从0开始)
     * @return {@link MultiUploadPartApi}
     */
    public MultiUploadPartApi from(byte[] buffer, int partIndex) {
        return from(buffer, 0, buffer == null ? 0 : buffer.length, partIndex);
    }

    /**
     * 配置分片的数据和序号
     *
     * @param buffer    分片数据
     * @param offset    分片数据偏移量
     * @param length    分片数据长度
     * @param partIndex 分片序号(从0开始)
     * @return {@link MultiUploadPartApi}
     */
    public MultiUploadPartApi from(byte[] buffer, int offset, int length, int partIndex) {
        this.buffer = buffer;
        this.partIndex = partIndex;
        this.offset = offset;
        this.length = length;
        return this;
    }

    /**
     * 配置是否需要计算并上传MD5
     *
     * @param isVerifyMd5 是否计算并上传MD5
     * @return {@link MultiUploadPartApi}
     */
    public MultiUploadPartApi withVerifyMd5(boolean isVerifyMd5) {
        this.isVerifyMd5 = isVerifyMd5;
        return this;
    }

    /**
     * 设置流读写的Buffer大小，默认 256 KB
     *
     * @param bufferSize Buffer大小
     * @return {@link MultiUploadPartApi}
     */
    public MultiUploadPartApi setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    /**
     * 配置进度回调间隔
     *
     * @param config 进度回调设置，{@link ProgressConfig}
     * @return {@link MultiUploadPartApi}
     */
    public MultiUploadPartApi withProgressConfig(ProgressConfig config) {
        progressConfig = config == null ? this.progressConfig : config;
        return this;
    }

    /**
     * 设置安全令牌（STS临时凭证）
     *
     * @param securityToken 安全令牌
     * @return {@link MultiUploadPartApi}
     */
    public MultiUploadPartApi withSecurityToken(String securityToken) {
        this.securityToken = securityToken;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public MultiUploadPartApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileClientException {
        parameterValidat();
        List<Parameter<String>> query = new ArrayList<>();
        query.add(new Parameter<>("uploadId", info.getUploadId()));
        query.add(new Parameter<>("partNumber", String.valueOf(partIndex)));

        contentType = MediaType.parse(info.getMimeType()).toString();
        String contentMD5 = "";
        String date = dateFormat.format(new Date(System.currentTimeMillis()));

        PutStreamRequestBuilder builder = new PutStreamRequestBuilder(onProgressListener)
                .setBufferSize(bufferSize);
        builder.baseUrl(builder.generateGetUrl(generateFinalHost(info.getBucket(), info.getKeyName()), query))
                .addHeader("Content-Type", contentType)
                .addHeader("Accpet", "*/*")
                .addHeader("Content-Length", String.valueOf(length))
                .addHeader("Date", date)
                .mediaType(MediaType.parse(info.getMimeType()));


        if (securityToken != null && !securityToken.isEmpty()) {
            builder.addHeader("SecurityToken", securityToken);
        }

        if (isVerifyMd5) {
            try {
                contentMD5 = HexFormatter.formatByteArray2HexString(Encoder.md5(buffer), false);
                builder.addHeader("Content-MD5", contentMD5);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.PUT, info.getBucket(), info.getKeyName(),
                contentType, contentMD5, date).setOptional(authOptionalData));

        builder.addHeader("authorization", authorization);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer, offset, length);
        builder.params(inputStream);
        builder.setProgressConfig(progressConfig);

        call = builder.build(httpClient.getOkHttpClient());
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (info == null)
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'info' can not be null");

        if (buffer == null || buffer.length == 0)
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'buffer' can not be null or empty");

        int len = buffer.length;
        if (offset < 0 || offset > (len - 1))
            throw new UfileParamException(
                    String.format("The offset you set %d, is out of data.length range[0,%d)", offset, len));

        if (!((offset + length) > 0))
            throw new UfileParamException(
                    String.format("The offset + length you set (%d + %d), is < 1", offset, length));

        if ((offset + length) > len)
            throw new UfileParamException(
                    String.format("The offset + length you set (%d + %d), is > data.length %d", offset, length, len));

        if (partIndex < 0)
            throw new UfileParamException(
                    "The required param 'partIndex' must be >= 0");
    }

    /**
     * 进度回调接口
     */
    private OnProgressListener onProgressListener;

    /**
     * 配置进度监听器
     * 该配置可供execute()同步接口回调进度使用，若使用executeAsync({@link BaseHttpCallback})，则后配置的会覆盖新配置的
     *
     * @param onProgressListener 进度监听器
     * @return {@link MultiUploadPartApi}
     */
    public MultiUploadPartApi setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
        return this;
    }

    @Override
    public void executeAsync(BaseHttpCallback callback) {
        onProgressListener = callback;
        super.executeAsync(callback);
    }

    @Override
    public MultiUploadPartState parseHttpResponse(Response response) throws UfileServerException, UfileClientException {
        try {
            MultiUploadPartState result = new MultiUploadPartState();
            String eTag = response.header("ETag", null);
            eTag = eTag == null ? null : eTag.replace("\"", "");
            result.seteTag(eTag);

            if (result.getPartIndex() == -1)
                result.setPartIndex(partIndex);

            return result;
        } finally {
            FileUtil.close(response.body());
        }
    }
}
