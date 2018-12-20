package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.bean.PutObjectResultBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileFileException;
import cn.ucloud.ufile.exception.UfileException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.BaseHttpCallback;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.ProgressConfig;
import cn.ucloud.ufile.http.request.PutFileRequestBuilder;
import cn.ucloud.ufile.util.*;
import com.google.gson.JsonElement;
import okhttp3.MediaType;
import okhttp3.Response;
import sun.security.validator.ValidatorException;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * API-Put上传小文件
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:08
 */
public class PutFileApi extends UfileObjectApi<PutObjectResultBean> {
    /**
     * Required
     * 云端对象名称
     */
    @NotEmpty(message = "KeyName is required to set through method 'nameAs'")
    protected String keyName;
    /**
     * Required
     * 要上传的文件
     */
    @NotNull(message = "File is required")
    private File file;
    /**
     * Required
     * 要上传的文件mimeType
     */
    @NotEmpty(message = "MimeType is required")
    protected String mimeType;
    /**
     * Required
     * Bucket空间名称
     */
    @NotEmpty(message = "BucketName is required to set through method 'toBucket'")
    protected String bucketName;
    /**
     * 是否需要上传MD5校验码
     */
    private boolean isVerifyMd5 = true;

    /**
     * 进度回调设置
     */
    private ProgressConfig progressConfig;

    /**
     * 构造方法
     *
     * @param authorizer Object授权器
     * @param host       API域名
     * @param httpClient Http客户端
     */
    protected PutFileApi(ObjectAuthorizer authorizer, String host, HttpClient httpClient) {
        super(authorizer, host, httpClient);
        progressConfig = ProgressConfig.callbackDefault();
    }

    /**
     * 设置上传到云端的对象名称
     *
     * @param keyName 对象名称
     * @return {@link PutFileApi}
     */
    public PutFileApi nameAs(String keyName) {
        this.keyName = keyName;
        return this;
    }

    /**
     * 设置要上传的文件和类型
     *
     * @param file     需上传的文件
     * @param mimeType 需上传文件的MIME类型
     * @return {@link PutFileApi}
     */
    public PutFileApi from(File file, String mimeType) {
        this.file = file;
        this.mimeType = mimeType;
        return this;
    }

    /**
     * 设置要上传到的Bucket名称
     *
     * @param bucketName bucket名称
     * @return {@link PutFileApi}
     */
    public PutFileApi toBucket(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 设置是否需要MD5校验
     *
     * @param isVerifyMd5 是否校验MD5
     * @return {@link PutFileApi}
     */
    public PutFileApi withVerifyMd5(boolean isVerifyMd5) {
        this.isVerifyMd5 = isVerifyMd5;
        return this;
    }

    /**
     * 配置进度回调设置
     *
     * @param config 进度回调设置
     * @return {@link PutFileApi}
     */
    public PutFileApi withProgressConfig(ProgressConfig config) {
        progressConfig = config == null ? this.progressConfig : config;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public PutFileApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileException {
        try {
            ParameterValidator.validator(this);
            if (!file.exists())
                throw new UfileFileException("Profile file is inexistent!");

            if (!file.isFile())
                throw new UfileFileException("Profile is not a file!");

            if (!file.canRead())
                throw new UfileFileException("Profile file is not readable!");

            String contentType = MediaType.parse(mimeType).toString();
            String contentMD5 = "";
            String date = dateFormat.format(new Date(System.currentTimeMillis()));

            PutFileRequestBuilder builder = (PutFileRequestBuilder) new PutFileRequestBuilder(onProgressListener)
                    .baseUrl(generateFinalHost(bucketName, keyName))
                    .addHeader("Content-Type", contentType)
                    .addHeader("Accpet", "*/*")
                    .addHeader("Content-Length", String.valueOf(file.length()))
                    .addHeader("Date", date)
                    .mediaType(MediaType.parse(mimeType));

            if (isVerifyMd5) {
                try {
                    contentMD5 = HexFormatter.formatByteArray2HexString(Encoder.md5(file), false);
                    builder.addHeader("Content-MD5", contentMD5);
                } catch (IOException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.PUT, bucketName, keyName,
                    contentType, contentMD5, date).setOptional(authOptionalData));
            builder.addHeader("authorization", authorization);

            builder.params(file);
            builder.setProgressConfig(progressConfig);

            call = builder.build(httpClient.getOkHttpClient());
        } catch (ValidatorException e) {
            throw new UfileRequiredParamNotFoundException(e.getMessage());
        }
    }

    private OnProgressListener onProgressListener;

    @Override
    public void executeAsync(BaseHttpCallback<PutObjectResultBean, UfileErrorBean> callback) {
        onProgressListener = callback;
        super.executeAsync(callback);
    }

    @Override
    public PutObjectResultBean parseHttpResponse(Response response) throws Exception {
        PutObjectResultBean result = super.parseHttpResponse(response);
        if (result != null && result.getRetCode() == 0)
            result.seteTag(response.header("ETag").replace("\"", ""));

        return result;
    }
}
