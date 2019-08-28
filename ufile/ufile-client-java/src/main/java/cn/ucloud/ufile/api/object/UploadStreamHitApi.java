package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.UfileConstants;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.auth.UfileAuthorizationException;
import cn.ucloud.ufile.auth.sign.UfileSignatureException;
import cn.ucloud.ufile.bean.base.BaseResponseBean;
import cn.ucloud.ufile.compat.base64.Base64UrlEncoderCompat;
import cn.ucloud.ufile.compat.base64.DefaultBase64UrlEncoderCompat;
import cn.ucloud.ufile.exception.UfileIOException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.PostJsonRequestBuilder;
import cn.ucloud.ufile.util.*;
import com.google.gson.JsonElement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * API-流秒传
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:08
 */
public class UploadStreamHitApi extends UfileObjectApi<BaseResponseBean> {
    /**
     * Required
     * 云端对象名称
     */
    protected String keyName;
    /**
     * Required
     * 要上传的流
     */
    protected InputStream inputStream;
    /**
     * Required
     * Bucket空间名称
     */
    protected String bucketName;
    /**
     * 兼容Java 1.8以下的Base64 编码器接口
     */
    @Deprecated
    private Base64UrlEncoderCompat base64;

    private ByteArrayOutputStream cacheOutputStream;

    /**
     * 构造方法
     *
     * @param authorizer Object授权器
     * @param host       API域名
     * @param httpClient Http客户端
     */
    protected UploadStreamHitApi(ObjectAuthorizer authorizer, String host, HttpClient httpClient) {
        super(authorizer, host, httpClient);
    }

    /**
     * 设置上传到云端的对象名称
     *
     * @param keyName 对象名称
     * @return {@link UploadStreamHitApi}
     */
    public UploadStreamHitApi nameAs(String keyName) {
        this.keyName = keyName;
        return this;
    }

    /**
     * 设置要上传的流
     *
     * @param inputStream 输入流
     * @return {@link UploadStreamHitApi}
     */
    public UploadStreamHitApi from(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    /**
     * 设置要上传到的Bucket名称
     *
     * @param bucketName bucket名称
     * @return {@link UploadStreamHitApi}
     */
    public UploadStreamHitApi toBucket(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 配置Base64 Url编码器，不调用该方法将会默认使用Java 1.8的Base64类
     * (若您的运行环境在Java 1.8以下，请使用该方法)
     *
     * @param base64 兼容Java 1.8以下的Base64 Url编码器接口
     * @return {@link UploadStreamHitApi}
     */
    @Deprecated
    public UploadStreamHitApi withBase64UrlEncoder(Base64UrlEncoderCompat base64) {
        this.base64 = base64;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public UploadStreamHitApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileParamException, UfileIOException, UfileAuthorizationException, UfileSignatureException {
        parameterValidat();

        String date = dateFormat.format(new Date(System.currentTimeMillis()));
        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.POST, bucketName, keyName,
                "", "", date).setOptional(authOptionalData));

        PostJsonRequestBuilder builder = new PostJsonRequestBuilder();

        String url = generateFinalHost(bucketName, "uploadhit");
        List<Parameter<String>> query = new ArrayList<>();

        backupStream();

        try {
            query.add(new Parameter<>("Hash", Etag.etag(new ByteArrayInputStream(cacheOutputStream.toByteArray())).geteTag()));
        } catch (IOException e) {
            throw new UfileIOException("Calculate ETag failed!", e);
        }

        query.add(new Parameter<>("FileName", keyName));
        query.add(new Parameter<>("FileSize", String.valueOf(new ByteArrayInputStream(cacheOutputStream.toByteArray()).available())));

        call = builder.baseUrl(builder.generateGetUrl(url, query))
                .setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut)
                .addHeader("Accpet", "*/*")
                .addHeader("Date", date)
                .addHeader("authorization", authorization)
                .build(httpClient.getOkHttpClient());
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (inputStream == null)
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'inputStream' can not be null");

        if (keyName == null || keyName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'keyName' can not be null or empty");

        if (bucketName == null || bucketName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'bucketName' can not be null or empty");
    }

    /**
     * 备份流
     */
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

}
