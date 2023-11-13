package cn.ucloud.ufile.api.object.multi;


import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.ObjectListApi;
import cn.ucloud.ufile.api.object.UfileObjectApi;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.bean.ObjectListBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.GetRequestBuilder;
import cn.ucloud.ufile.http.request.PostJsonRequestBuilder;
import cn.ucloud.ufile.util.FileUtil;
import cn.ucloud.ufile.util.HttpMethod;
import cn.ucloud.ufile.util.Parameter;
import okhttp3.Response;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * API-初始化分片上传
 *
 * @author: leo
 * @E-mail: leo.song@ucloud.cn
 * @date: 2023/11/09 19:08
 */
public class MultiUploadListPartsInfoApi extends UfileObjectApi<MultiUploadListPartsInfo> {
    /**
     * Required
     * Bucket
     */
    protected String bucket;
    /**
     * Required
     * Upload id
     */
    protected String uploadId;

    /**
     * 规定在US3响应中的最大Part数目
     */
    protected Integer maxParts;

    /**
     * 指定List的起始位置，只有Part Number数目大于该参数的Part会被列出
     */
    protected Integer partNumberMarker;

    /**
     * 构造方法
     *
     * @param authorizer   Object授权器
     * @param objectConfig ObjectConfig {@link ObjectConfig}
     * @param httpClient   Http客户端
     */
    public MultiUploadListPartsInfoApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
    }

    /**
     * 配置Upload id
     *
     * @param uploadId uploadId
     * @return {@link MultiUploadListPartsInfoApi}
     */
    public MultiUploadListPartsInfoApi setUploadId(String uploadId) {
        this.uploadId = uploadId;
        return this;
    }

    /**
     * 配置指定Bucket
     *
     * @param bucketName bucket名称
     * @return {@link ObjectListApi}
     */
    public MultiUploadListPartsInfoApi atBucket(String bucketName) {
        this.bucket = bucketName;
        return this;
    }

    /**
     * 配置Upload id
     *
     * @param uploadId uploadId
     * @param maxParts maxParts
     * @param partNumberMarker partNumberMarker
     * @return {@link MultiUploadListPartsInfoApi}
     */
    public MultiUploadListPartsInfoApi uploadId(String uploadId, Integer maxParts, Integer partNumberMarker) {
        this.uploadId = uploadId;
        this.maxParts = maxParts;
        this.partNumberMarker = partNumberMarker;
        return this;
    }
    /**
     * 配置Upload id
     *
     * @param maxParts 规定在US3响应中的最大Part数目
     * @return {@link MultiUploadListPartsInfoApi}
     */
    public MultiUploadListPartsInfoApi maxParts(Integer maxParts) {
        this.maxParts = maxParts;
        return this;
    }
    /**
     * 配置分片上传到云端的对象的MIME类型
     *
     * @param partNumberMarker  指定List的起始位置，只有Part Number数目大于该参数的Part会被列出
     * @return {@link InitMultiUploadApi}
     */
    public MultiUploadListPartsInfoApi partNumberMarker(Integer partNumberMarker) {
        this.partNumberMarker = partNumberMarker;
        return this;
    }

    @Override
    protected void prepareData() throws UfileClientException {
        parameterValidat();
        List<Parameter<String>> query = new ArrayList<>();
        if (uploadId != null)
            query.add(new Parameter<>("uploadId", uploadId));
        if (partNumberMarker != null)
            query.add(new Parameter<>("part-number-marker", String.valueOf(partNumberMarker.intValue())));
        if (maxParts != null)
            query.add(new Parameter<>("max-parts", String.valueOf(maxParts.intValue())));

        contentType = "application/json; charset=utf-8";
        String date = dateFormat.format(new Date(System.currentTimeMillis()));

        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.GET, bucket, "",
                contentType, "", date).setOptional(authOptionalData));

        GetRequestBuilder builder = new GetRequestBuilder();
        call = builder.baseUrl(generateFinalHost(bucket, "")+ "?muploadpart&" + builder.generateUrlQuery(query))
                .setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut)
                .addHeader("Content-Type", contentType)
                .addHeader("Accpet", "*/*")
                .addHeader("Date", date)
                .addHeader("authorization", authorization)
                .build(httpClient.getOkHttpClient());
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (uploadId == null || uploadId.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'uploadId' can not be null or empty");

    }
    @Override
    public MultiUploadListPartsInfo parseHttpResponse(Response response) throws UfileClientException, UfileServerException {
        try {
            MultiUploadListPartsInfo result = super.parseHttpResponse(response);

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
}
