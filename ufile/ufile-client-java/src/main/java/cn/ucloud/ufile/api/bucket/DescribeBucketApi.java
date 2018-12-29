package cn.ucloud.ufile.api.bucket;

import cn.ucloud.ufile.annotation.UcloudParam;
import cn.ucloud.ufile.auth.BucketAuthorizer;
import cn.ucloud.ufile.bean.BucketDescribeResponse;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.HttpClient;

/**
 * API-拉取Bucket信息
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:00
 */
public class DescribeBucketApi extends UfileBucketApi<BucketDescribeResponse> {
    /**
     * Optional
     * 指定的Bucket名称
     */
    @UcloudParam("BucketName")
    private String bucketName;

    /**
     * Optional
     * 查询结果的偏移量，default = 0
     */
    @UcloudParam("Offset")
    private String offset = "0";

    /**
     * Optional
     * 查询结果的分页长度，default = 20
     */
    @UcloudParam("Limit")
    private String limit = "20";

    /**
     * Optional
     * 相关联的项目ID
     */
    @UcloudParam("ProjectId")
    private String projectId;

    /**
     * 构造方法
     *
     * @param authorizer Bucket授权器
     * @param httpClient Http客户端
     */
    protected DescribeBucketApi(BucketAuthorizer authorizer, HttpClient httpClient) {
        super(authorizer, httpClient, "DescribeBucket");
    }

    /**
     * 配置要查询的bucket名称
     *
     * @param bucketName bucket名称
     * @return {@link DescribeBucketApi}
     */
    public DescribeBucketApi whichBucket(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 配置要查询的bucket所属UCloud的projectId
     *
     * @param projectId projectId
     * @return {@link DescribeBucketApi}
     */
    public DescribeBucketApi withProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    /**
     * 配置查询结果分页起始偏移量和数据长度
     *
     * @param offset 偏移量
     * @param limit  数据长度
     * @return {@link DescribeBucketApi}
     */
    public DescribeBucketApi withOffsetAndLimit(int offset, int limit) {
        this.offset = String.valueOf(offset);
        this.limit = String.valueOf(limit);
        return this;
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        super.parameterValidat();
    }
}
