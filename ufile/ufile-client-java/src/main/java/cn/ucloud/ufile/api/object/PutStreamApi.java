package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.bean.PutObjectResultBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileException;
import cn.ucloud.ufile.exception.UfileIOException;
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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * API-Put上传流
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:08
 */
public class PutStreamApi extends UfileObjectApi<PutObjectResultBean> {
    /**
     * Required
     * 云端对象名称
     */
    @NotEmpty(message = "KeyName is required to set through method 'nameAs'")
    protected String keyName;
    /**
     * Required
     * 要上传的流
     */
    @NotNull(message = "InputStream is required")
    protected InputStream inputStream;
    /**
     * Required
     * 要上传的流mimeType
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

    private ProgressConfig progressConfig;

    private ByteArrayOutputStream cacheOutputStream;

    /**
     * 构造方法
     *
     * @param authorizer Object授权器
     * @param host       API域名
     * @param httpClient Http客户端
     */
    protected PutStreamApi(ObjectAuthorizer authorizer, String host, HttpClient httpClient) {
        super(authorizer, host, httpClient);
        progressConfig = ProgressConfig.callbackDefault();
    }

    /**
     * 设置上传到云端的对象名称
     *
     * @param keyName 对象名称
     * @return {@link PutStreamApi}
     */
    public PutStreamApi nameAs(String keyName) {
        this.keyName = keyName;
        return this;
    }

    /**
     * 设置要上传的流和MIME类型
     *
     * @param inputStream 输入流
     * @param mimeType    MIME类型
     * @return {@link PutStreamApi}
     */
    public PutStreamApi from(InputStream inputStream, String mimeType) {
        this.inputStream = inputStream;
        this.mimeType = mimeType;
        return this;
    }

    /**
     * 设置要上传到的Bucket名称
     *
     * @param bucketName bucket名称
     * @return
     */
    public PutStreamApi toBucket(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 设置是否需要MD5校验
     *
     * @param isVerifyMd5 是否校验MD5
     * @return {@link PutStreamApi}
     */
    public PutStreamApi withVerifyMd5(boolean isVerifyMd5) {
        this.isVerifyMd5 = isVerifyMd5;
        return this;
    }

    /**
     * 配置进度回调设置
     *
     * @param config 进度回调设置
     * @return {@link PutStreamApi}
     */
    public PutStreamApi withProgressConfig(ProgressConfig config) {
        progressConfig = config == null ? this.progressConfig : config;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public PutStreamApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileException {
        try {
            ParameterValidator.validator(this);

            String contentType = MediaType.parse(mimeType).toString();
            String contentMD5 = "";
            String date = dateFormat.format(new Date(System.currentTimeMillis()));

            PutStreamRequestBuilder builder = (PutStreamRequestBuilder) new PutStreamRequestBuilder(onProgressListener)
                    .baseUrl(generateFinalHost(bucketName, keyName))
                    .addHeader("Content-Type", contentType)
                    .addHeader("Accpet", "*/*")
                    .addHeader("Date", date)
                    .mediaType(MediaType.parse(mimeType));

            builder.addHeader("Content-Length", String.valueOf(inputStream.available()));

            if (isVerifyMd5) {
                try {
                    backupStream();
                    contentMD5 = HexFormatter.formatByteArray2HexString(Encoder.md5(new ByteArrayInputStream(cacheOutputStream.toByteArray())), false);
                    builder.addHeader("Content-MD5", contentMD5);
                    inputStream = new ByteArrayInputStream(cacheOutputStream.toByteArray());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.PUT, bucketName, keyName,
                    contentType, contentMD5, date).setOptional(authOptionalData));
            builder.addHeader("authorization", authorization);

            builder.params(inputStream);
            builder.setProgressConfig(progressConfig);

            FileUtil.close(cacheOutputStream);

            call = builder.build(httpClient.getOkHttpClient());
        } catch (ValidatorException e) {
            throw new UfileRequiredParamNotFoundException(e.getMessage());
        } catch (IOException e) {
            throw new UfileIOException(e.getMessage());
        }
    }

    private void backupStream() {
        cacheOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[64 * 1024];
        int len = 0;
        try {
            while ((len = inputStream.read(buff)) > 0) {
                cacheOutputStream.write(buff, 0, len);
            }

            cacheOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(inputStream);
        }
    }

    private OnProgressListener onProgressListener;

    /**
     * 配置进度监听器
     * 该配置可供execute()同步接口回调进度使用，若使用executeAsync({@link BaseHttpCallback})，则后配置的会覆盖新配置的
     *
     * @param onProgressListener 进度监听器
     * @return {@link PutStreamApi}
     */
    public PutStreamApi setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
        return this;
    }

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
