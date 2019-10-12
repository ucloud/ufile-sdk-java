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
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * API-云端对象文件重命名
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:08
 */
public class RenameObjectApi extends UfileObjectApi<BaseResponseBean> {

    /**
     * Required
     * Bucket空间名称
     */
    protected String bucketName;

    /**
     * Required
     * 云端对象原名称
     */
    protected String keyName;

    /**
     * Required
     * 云端对象新名称
     */
    protected String newKeyName;

    /**
     * 是否强制覆盖
     * 如果已存在同名文件，值为true则覆盖，否则操作失败；请求中若不携带该参数，默认不覆盖
     */
    protected boolean isForce;

    /**
     * 构造方法
     *
     * @param authorizer Object授权器
     * @param host       API域名
     * @param httpClient Http客户端
     */
    protected RenameObjectApi(ObjectAuthorizer authorizer, String host, HttpClient httpClient) {
        super(authorizer, host, httpClient);
    }

    /**
     * 设置要重命名的源对象信息
     *
     * @param bucketName 要重命名的源对象bucket名称
     * @param keyName    要重命名的源对象文件名称
     * @return {@link RenameObjectApi}
     */
    public RenameObjectApi which(String bucketName, String keyName) {
        this.bucketName = bucketName;
        this.keyName = keyName;
        return this;
    }

    /**
     * 设置要重命名的新名称
     *
     * @param newKeyName 新名称
     * @return {@link RenameObjectApi}
     */
    public RenameObjectApi isRenamedTo(String newKeyName) {
        this.newKeyName = newKeyName;
        return this;
    }

    /**
     * 设置是否强制覆盖同名文件
     * 如果已存在同名文件，值为true则覆盖，否则操作失败；请求中若不携带该参数，默认不覆盖
     *
     * @param isForce 是否强制覆盖同名文件
     * @return {@link RenameObjectApi}
     */
    public RenameObjectApi isForcedToCover(boolean isForce) {
        this.isForce = isForce;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public RenameObjectApi withAuthOptionalData(JsonElement authOptionalData) {
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
        params.add(new Parameter<>("newFileName", newKeyName));
        params.add(new Parameter<>("force", String.valueOf(isForce)));

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

        if (newKeyName == null || newKeyName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'newKeyName' can not be null or empty");
    }
}
