package cn.ucloud.ufile.api.bucket;

import cn.ucloud.ufile.annotation.UcloudParam;
import cn.ucloud.ufile.auth.BucketAuthorizer;
import cn.ucloud.ufile.bean.BucketResponse;
import cn.ucloud.ufile.http.HttpClient;

import javax.validation.constraints.NotEmpty;

/**
 * API-创建Bucket
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 18:57
 */
public class CreateBucketApi extends UfileBucketApi<BucketResponse> {
    /**
     * Required
     * 要创建的Bucket名称
     */
    @NotEmpty(message = "BucketName is required")
    @UcloudParam("BucketName")
    private String bucketName;

    /**
     * Required
     * 要创建的Bucket类型
     */
    @NotEmpty(message = "Type is required")
    @UcloudParam("Type")
    private String type;

    /**
     * Required
     * 要创建Bucket的所在地区
     */
    @NotEmpty(message = "Region is required")
    @UcloudParam("Region")
    private String region;

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
    protected CreateBucketApi(BucketAuthorizer authorizer, HttpClient httpClient) {
        super(authorizer, httpClient, "CreateBucket");
    }

    /**
     * 配置新建的bucket名称
     *
     * @param bucketName
     * @return {@link CreateBucketApi}
     */
    public CreateBucketApi bucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 配置新建的bucket的类型
     *
     * @param type bucket类型
     * @return {@link CreateBucketApi}
     */
    public CreateBucketApi withType(BucketType type) {
        this.type = type.getBucketType();
        return this;
    }

    /**
     * 配置新建的bucket所属地区
     *
     * @param region 地区
     * @return {@link CreateBucketApi}
     */
    public CreateBucketApi atRegion(String region) {
        this.region = region;
        return this;
    }

    /**
     * 配置新建的bucket所属UCloud的projectId
     *
     * @param projectId projectId
     * @return {@link CreateBucketApi}
     */
    public CreateBucketApi withProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

}
