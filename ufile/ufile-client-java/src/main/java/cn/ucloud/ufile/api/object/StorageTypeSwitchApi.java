package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.bean.base.BaseResponseBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.PutJsonRequestBuilder;
import cn.ucloud.ufile.util.HttpMethod;
import cn.ucloud.ufile.util.Parameter;
import cn.ucloud.ufile.util.StorageType;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * API-云端对象文件转换存储类型
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:08
 */
public class StorageTypeSwitchApi extends UfileObjectApi<BaseResponseBean> {

    /**
     * Required
     * Bucket空间名称
     */
    protected String bucketName;

    /**
     * Required
     * 云端对象名称
     */
    protected String keyName;

    /**
     * Required
     * 要修改的存储类型 {@link StorageType}
     */
    protected String storageType;

    /**
     * 构造方法
     *
     * @param authorizer Object授权器
     * @param host       API域名
     * @param httpClient Http客户端
     */
    protected StorageTypeSwitchApi(ObjectAuthorizer authorizer, String host, HttpClient httpClient) {
        super(authorizer, host, httpClient);
    }

    /**
     * 设置要转换存储类型的对象信息
     *
     * @param bucketName 要重命名的源对象bucket名称
     * @param keyName    要重命名的源对象文件名称
     * @return {@link StorageTypeSwitchApi}
     */
    public StorageTypeSwitchApi which(String bucketName, String keyName) {
        this.bucketName = bucketName;
        this.keyName = keyName;
        return this;
    }

    /**
     * 设置要转换的存储类型
     *
     * @param storageType 存储类型，详情请见{@link StorageType}
     * @return {@link StorageTypeSwitchApi}
     */
    public StorageTypeSwitchApi turnTypeTo(String storageType) {
        this.storageType = storageType;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public StorageTypeSwitchApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileClientException {
        parameterValidat();

        String contentType = "application/json; charset=utf-8";
        String contentMD5 = "";
        String date = dateFormat.format(new Date(System.currentTimeMillis()));

        PutJsonRequestBuilder builder = (PutJsonRequestBuilder) new PutJsonRequestBuilder()
                .setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut);

        List<Parameter<String>> params = new ArrayList<>();
        params.add(new Parameter<>("storageClass", storageType));

        builder.baseUrl(builder.generateGetUrl(generateFinalHost(bucketName, keyName), params))
                .addHeader("Content-Type", contentType)
                .addHeader("Accpet", "*/*")
                .addHeader("Date", date);

        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.PUT,
                bucketName, keyName, contentType, contentMD5, date).setOptional(authOptionalData));

        builder.addHeader("authorization", authorization);

        call = builder.build(httpClient.getOkHttpClient());
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (bucketName == null || bucketName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'srcBucketName' can not be null or empty");

        if (keyName == null || keyName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'srcKeyName' can not be null or empty");

        if (storageType == null || storageType.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'storageType' can not be null or empty");
    }
}
