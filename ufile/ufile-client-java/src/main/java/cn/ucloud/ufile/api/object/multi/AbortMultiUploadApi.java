package cn.ucloud.ufile.api.object.multi;

import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.UfileObjectApi;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.bean.base.BaseObjectResponseBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.DeleteRequestBuilder;
import cn.ucloud.ufile.util.HttpMethod;
import cn.ucloud.ufile.util.Parameter;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * API-中断分片上传
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:08
 */
public class AbortMultiUploadApi extends UfileObjectApi<BaseObjectResponseBean> {
    /**
     * Required
     * 分片上传初始化状态
     */
    private MultiUploadInfo info;
    
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
    public AbortMultiUploadApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
    }

    /**
     * 配置需要终止的分片上传任务
     *
     * @param info 分片上传初始化信息，{@link MultiUploadInfo}
     * @return {@link AbortMultiUploadApi}
     */
    public AbortMultiUploadApi which(MultiUploadInfo info) {
        this.info = info;
        return this;
    }

    /**
     * 设置安全令牌（STS临时凭证）
     *
     * @param securityToken 安全令牌
     * @return {@link AbortMultiUploadApi}
     */
    public AbortMultiUploadApi withSecurityToken(String securityToken) {
        this.securityToken = securityToken;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public AbortMultiUploadApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileClientException {
        parameterValidat();

        contentType = "application/json; charset=utf-8";
        String date = dateFormat.format(new Date(System.currentTimeMillis()));

        String authorization = authorizer.authorization((ObjectOptAuthParam)
                new ObjectOptAuthParam(HttpMethod.DELETE, info.getBucket(), info.getKeyName(),
                        contentType, "", date).setOptional(authOptionalData));

        DeleteRequestBuilder builder = new DeleteRequestBuilder();
        List<Parameter<String>> query = new ArrayList<>();
        query.add(new Parameter<>("uploadId", info.getUploadId()));

        builder.baseUrl(builder.generateGetUrl(generateFinalHost(info.getBucket(), info.getKeyName()), query))
                .addHeader("Content-Type", contentType)
                .addHeader("Accpet", "*/*")
                .addHeader("Date", date)
                .addHeader("authorization", authorization);
                

        if (securityToken != null && !securityToken.isEmpty()) {
            builder.addHeader("SecurityToken", securityToken);
        }

        call = builder.build(httpClient.getOkHttpClient());
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (info == null)
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'info' can not be null");
    }
}
