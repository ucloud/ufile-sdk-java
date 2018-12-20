package cn.ucloud.ufile.api.object.multi;

import cn.ucloud.ufile.api.object.GenerateObjectPrivateUrlApi;
import cn.ucloud.ufile.api.object.UfileObjectApi;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.exception.UfileException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.BaseHttpCallback;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.ProgressConfig;
import cn.ucloud.ufile.http.request.PutStreamRequestBuilder;
import cn.ucloud.ufile.util.*;
import com.google.gson.JsonElement;
import okhttp3.MediaType;
import okhttp3.Response;
import sun.security.validator.ValidatorException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
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
    @NotNull(message = "Info is required to set through method 'which'")
    private MultiUploadInfo info;
    /**
     * Required
     * 需要上传的分片数据
     */
    @NotNull(message = "Buffer is required")
    private byte[] buffer;
    /**
     * Required
     * 该分片的序号
     */
    @PositiveOrZero(message = "PartIndex must > 0")
    private int partIndex;
    /**
     * 是否需要上传MD5校验码
     */
    private boolean isVerifyMd5 = true;

    /**
     * 进度回调配置
     */
    private ProgressConfig progressConfig;

    /**
     * 构造方法
     *
     * @param authorizer Object授权器
     * @param host       API域名
     * @param httpClient Http客户端
     */
    public MultiUploadPartApi(ObjectAuthorizer authorizer, String host, HttpClient httpClient) {
        super(authorizer, host, httpClient);
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
        this.buffer = buffer;
        this.partIndex = partIndex;
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
    protected void prepareData() throws UfileException {
        try {
            ParameterValidator.validator(this);
            List<Parameter<String>> query = new ArrayList<>();
            query.add(new Parameter<>("uploadId", info.getUploadId()));
            query.add(new Parameter<>("partNumber", String.valueOf(partIndex)));

            String contentType = MediaType.parse(info.getMimeType()).toString();
            String contentMD5 = "";
            String date = dateFormat.format(new Date(System.currentTimeMillis()));

            PutStreamRequestBuilder builder = new PutStreamRequestBuilder(onProgressListener);
            builder.baseUrl(builder.generateGetUrl(generateFinalHost(info.getBucket(), info.getKeyName()), query))
                    .addHeader("Content-Type", contentType)
                    .addHeader("Accpet", "*/*")
                    .addHeader("Content-Length", String.valueOf(buffer.length))
                    .addHeader("Date", date)
                    .mediaType(MediaType.parse(info.getMimeType()));

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

            builder.params(new ByteArrayInputStream(buffer));
            builder.setProgressConfig(progressConfig);

            call = builder.build(httpClient.getOkHttpClient());
        } catch (ValidatorException e) {
            throw new UfileRequiredParamNotFoundException(e.getMessage());
        }
    }

    /**
     * 进度回调接口
     */
    private OnProgressListener onProgressListener;

    @Override
    public void executeAsync(BaseHttpCallback callback) {
        onProgressListener = callback;
        super.executeAsync(callback);
    }

    @Override
    public MultiUploadPartState parseHttpResponse(Response response) throws Exception {
        MultiUploadPartState result = super.parseHttpResponse(response);
        if (result != null && result.getRetCode() == 0)
            result.seteTag(response.header("ETag").replace("\"", ""));
        if (result.getPartIndex() == -1)
            result.setPartIndex(partIndex);

        return result;
    }
}
