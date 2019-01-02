package cn.ucloud.ufile.api.bucket;

import cn.ucloud.ufile.annotation.UcloudParam;
import cn.ucloud.ufile.auth.BucketAuthorizer;
import cn.ucloud.ufile.bean.BucketResponse;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.HttpClient;

/**
 * API-删除Bucket
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 18:58
 */
public class DeleteBucketApi extends UfileBucketApi<BucketResponse> {
    /**
     * Required
     * 要删除的Bucket名称
     */
    @UcloudParam("BucketName")
    private String bucketName;

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
    protected DeleteBucketApi(BucketAuthorizer authorizer, HttpClient httpClient) {
        super(authorizer, httpClient, "DeleteBucket");
    }

    /**
     * 配置要删除的bucket名称
     *
     * @param bucketName bucket名称
     * @return {@link DeleteBucketApi}
     */
    public DeleteBucketApi whichBucket(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 配置要删除的bucket所属UCloud的projectId
     *
     * @param projectId projectId
     * @return {@link DeleteBucketApi}
     */
    public DeleteBucketApi withProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        super.parameterValidat();
        if (bucketName == null || bucketName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'bucketName' can not be null or empty");
    }
}
