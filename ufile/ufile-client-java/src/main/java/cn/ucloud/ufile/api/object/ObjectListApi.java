package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.auth.UfileAuthorizationException;
import cn.ucloud.ufile.bean.ObjectListBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.GetRequestBuilder;
import cn.ucloud.ufile.util.FileUtil;
import cn.ucloud.ufile.util.HttpMethod;
import cn.ucloud.ufile.util.Parameter;
import com.google.gson.JsonElement;
import okhttp3.Response;

import java.util.*;

/**
 * API-获取云端对象列表
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:09
 */
public class ObjectListApi extends UfileObjectApi<ObjectListBean> {
    /**
     * Optional
     * 过滤前缀
     */
    private String prefix;
    /**
     * Optional
     * 分页标记
     */
    private String marker;
    /**
     * Optional
     * 分页数据上限，Default = 20
     */
    private Integer limit;
    
    /**
     * Optional
     * 目录分隔符
     */
    private String delimiter;

    /**
     * Required
     * Bucket空间名称
     */
    private String bucketName;

    /**
     * Optional
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
    protected ObjectListApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
    }

    /**
     * 配置指定Bucket
     *
     * @param bucketName bucket名称
     * @return {@link ObjectListApi}
     */
    public ObjectListApi atBucket(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 配置过滤前缀
     *
     * @param prefix 前缀
     * @return {@link ObjectListApi}
     */
    public ObjectListApi withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * 配置分页标记
     *
     * @param marker 分页标记
     * @return {@link ObjectListApi}
     */
    public ObjectListApi withMarker(String marker) {
        this.marker = marker;
        return this;
    }

    /**
     * 配置分页数据长度
     *
     * @param limit 分页数据长度
     * @return {@link ObjectListApi}
     */
    public ObjectListApi dataLimit(int limit) {
        this.limit = limit;
        return this;
    }
    
    /**
     * 配置目录分隔符
     *
     * @param delimiter 目录分隔符
     * @return {@link ObjectListApi}
     */
    public ObjectListApi withDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * 配置安全令牌（STS临时凭证）
     *
     * @param securityToken 安全令牌
     * @return {@link ObjectListApi}
     */
    public ObjectListApi withSecurityToken(String securityToken) {
        this.securityToken = securityToken;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public ObjectListApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileClientException {
        parameterValidat();
        List<Parameter<String>> query = new ArrayList<>();
        if (prefix != null)
            query.add(new Parameter<>("prefix", prefix));
        if (marker != null)
            query.add(new Parameter<>("marker", marker));
        if (limit != null)
            query.add(new Parameter<>("limit", String.valueOf(limit.intValue())));
        if (delimiter != null)
            query.add(new Parameter<>("delimiter", delimiter));

        contentType = "application/json; charset=utf-8";
        String date = dateFormat.format(new Date(System.currentTimeMillis()));

        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.GET, bucketName, "",
                contentType, "", date).setOptional(authOptionalData));

        GetRequestBuilder builder = new GetRequestBuilder();
        builder.baseUrl(generateFinalHost(bucketName, "") + "?list&" + builder.generateUrlQuery(query))
                .setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut)
                .header(headers)
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
    public ObjectListBean parseHttpResponse(Response response) throws UfileClientException, UfileServerException {
        try {
            ObjectListBean result = super.parseHttpResponse(response);

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

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (bucketName == null || bucketName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'bucketName' can not be null or empty");

        if (limit != null && limit.intValue() < 1)
            throw new UfileParamException(
                    "The required param 'limit' must be > 0");
    }
}
