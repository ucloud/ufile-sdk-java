package cn.ucloud.ufile.api.object.multi;

import cn.ucloud.ufile.UfileConstants;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.UfileObjectApi;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.PutJsonRequestBuilder;
import cn.ucloud.ufile.util.*;
import okhttp3.MediaType;

import java.util.*;

/**
 * API-分片拷贝上传
 *
 * @author: kenny
 * @E-mail: kenny.wang@ucloud.cn
 * @date: 2023/06/27 15:08
 */
public class UploadCopyPartApi extends UfileObjectApi<MultiUploadPartState> {
    /**
     * Required
     * 分片上传初始化状态
     */
    private MultiUploadInfo info;
    /**
     * Required
     * 文件源BucketName
     */
    private String sourceBucketName;
    /**
     * Required
     * 文件源ObjectName
     */
    private String sourceObjectName;
    /**
     * Required
     * Range开始的Index
     */
    private long rangeStart;
    /**
     * Required
     * Range结束的Index
     */
    private long rangeEnd;
    /**
     * Required
     * 该分片的序号
     */
    private int partIndex;

    /**
     * 构造方法
     *
     * @param authorizer   Object授权器
     * @param objectConfig ObjectConfig {@link ObjectConfig}
     * @param httpClient   Http客户端
     */
    public UploadCopyPartApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
    }

    /**
     * 配置需要上传的分片上传任务
     *
     * @param info 分片上传初始化信息，{@link MultiUploadInfo}
     * @return {@link UploadCopyPartApi}
     */
    public UploadCopyPartApi which(MultiUploadInfo info) {
        this.info = info;
        return this;
    }

    /**
     * 配置分片的数据和序号
     *
     * @param partIndex        分片序号(从0开始)
     * @param sourceBucketName 文件源BucketName
     * @param sourceObjectName 文件源ObjectName
     * @param rangeStart       Range开始的Index
     * @param rangeEnd         Range结束的Index
     * @return {@link UploadCopyPartApi}
     */
    public UploadCopyPartApi from(int partIndex, String sourceBucketName, String sourceObjectName, long rangeStart, long rangeEnd) {
        this.partIndex = partIndex;
        this.sourceBucketName = sourceBucketName;
        this.sourceObjectName = sourceObjectName;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        return this;
    }

    @Override
    protected void prepareData() throws UfileClientException {
        parameterValidat();
        List<Parameter<String>> query = new ArrayList<>();
        query.add(new Parameter<>("uploadId", info.getUploadId()));
        query.add(new Parameter<>("partNumber", String.valueOf(partIndex)));

        String date = dateFormat.format(new Date(System.currentTimeMillis()));

        String xUfileCopySource = String.format("/%s/%s", sourceBucketName, sourceObjectName);
        String xUfileCopySourceRange = String.format("bytes=%d-%d", rangeStart, rangeEnd);

        PutJsonRequestBuilder builder = new PutJsonRequestBuilder();
        builder.baseUrl(builder.generateGetUrl(generateFinalHost(info.getBucket(), info.getKeyName()), query))
                .addHeader("Accpet", "*/*")
                .addHeader("Date", date)
                .addHeader("X-Ufile-Copy-Source", xUfileCopySource)
                .addHeader("X-Ufile-Copy-Source-Range", xUfileCopySourceRange);

        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.PUT, info.getBucket(), info.getKeyName(),
                contentType, "", date, xUfileCopySource, xUfileCopySourceRange).setOptional(authOptionalData));
        builder.addHeader("authorization", authorization);

        call = builder.build(httpClient.getOkHttpClient());
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (info == null)
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'info' can not be null");

        if ("".equals(sourceBucketName) || "".equals(sourceObjectName))
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'sourceBucketName','sourceObjectName' can not be empty");

        if (rangeEnd - rangeStart > UfileConstants.MULTIPART_SIZE - 1)
            throw new UfileParamException(
                    String.format("The rangeStart,rangeEnd you set %d,%d, sub is out of MULTIPART_SIZE(%d)", rangeStart, rangeEnd, UfileConstants.MULTIPART_SIZE));
    }
}
