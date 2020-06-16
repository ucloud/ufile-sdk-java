package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.bean.ObjectContentBean;
import cn.ucloud.ufile.bean.ObjectListWithDirFormatBean;
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
import com.google.gson.JsonObject;
import okhttp3.Response;

import java.util.*;

/**
 * API-获取目录格式的云端对象列表
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2019/09/26 19:09
 */
public class ObjectListWithDirFormatApi extends UfileObjectApi<ObjectListWithDirFormatBean> {

    /**
     * Required
     * Bucket空间名称
     */
    private String bucketName;

    /**
     * Optional
     * 返回以Prefix作为前缀的目录文件列表
     */
    private String prefix;

    /**
     * Optional
     * 返回以字母排序后，大于Marker的目录文件列表
     */
    private String marker;

    /**
     * Optional
     * 指定返回目录文件列表的最大数量，默认值为100，不超过1000
     */
    private Integer limit;

    /**
     * Optional
     * 目录分隔符，默认为'/'，当前只支持是'/'
     */
    private String delimiter;

    /**
     * 构造方法
     *
     * @param authorizer   Object授权器
     * @param objectConfig ObjectConfig {@link ObjectConfig}
     * @param httpClient   Http客户端
     */
    protected ObjectListWithDirFormatApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
    }

    /**
     * 配置指定Bucket
     *
     * @param bucketName bucket名称
     * @return {@link ObjectListWithDirFormatApi}
     */
    public ObjectListWithDirFormatApi atBucket(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 配置过滤前缀
     *
     * @param prefix 前缀
     * @return {@link ObjectListWithDirFormatApi}
     */
    public ObjectListWithDirFormatApi withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * 配置分页标记
     *
     * @param marker 分页标记
     * @return {@link ObjectListWithDirFormatApi}
     */
    public ObjectListWithDirFormatApi withMarker(String marker) {
        this.marker = marker;
        return this;
    }

    /**
     * 配置分页数据长度
     *
     * @param limit 分页数据长度：Default = 100，<= 1000
     * @return {@link ObjectListWithDirFormatApi}
     */
    public ObjectListWithDirFormatApi dataLimit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * 配置目录分隔符
     *
     * @param delimiter 分页数据长度：Default = '/'，当前只支持是'/'
     * @return {@link ObjectListWithDirFormatApi}
     */
    public ObjectListWithDirFormatApi withDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return {@link ObjectListWithDirFormatApi}
     */
    public ObjectListWithDirFormatApi withAuthOptionalData(JsonElement authOptionalData) {
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
            query.add(new Parameter<>("max-keys", String.valueOf(limit.intValue())));
        if (delimiter != null)
            query.add(new Parameter<>("delimiter", delimiter));

        contentType = "application/json; charset=utf-8";
        String date = dateFormat.format(new Date(System.currentTimeMillis()));

        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.GET, bucketName, "",
                contentType, "", date).setOptional(authOptionalData));

        GetRequestBuilder builder = new GetRequestBuilder();
        call = builder.baseUrl(generateFinalHost(bucketName, "") + "?listobjects&" + builder.generateUrlQuery(query))
                .setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut)
                .addHeader("Content-Type", contentType)
                .addHeader("Accpet", "*/*")
                .addHeader("Date", date)
                .addHeader("authorization", authorization)
                .build(httpClient.getOkHttpClient());
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

    @Override
    public ObjectListWithDirFormatBean parseHttpResponse(Response response) throws UfileClientException, UfileServerException {
        try {
            ObjectListWithDirFormatBean result = super.parseHttpResponse(response);
            if (result != null) {
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

                List<ObjectContentBean> contents = result.getObjectContents();
                if (contents != null && !contents.isEmpty()) {
                    for (ObjectContentBean content : contents) {
                        if (content == null || content.getJsonUserMeta() == null)
                            continue;

                        JsonElement json = content.getJsonUserMeta();
                        if (json != null && json instanceof JsonObject) {
                            JsonObject jsonObj = (JsonObject) json;
                            Set<String> keys = jsonObj.keySet();
                            if (keys != null) {
                                Map<String, String> metadata = new HashMap<>();
                                for (String name : keys) {
                                    if (name == null || name.isEmpty())
                                        continue;

                                    metadata.put(name.toLowerCase(), jsonObj.get(name).getAsString());
                                }
                                content.setUserMeta(metadata);
                            }
                        }
                    }
                }
            }

            return result;
        } finally {
            FileUtil.close(response.body());
        }
    }
}
