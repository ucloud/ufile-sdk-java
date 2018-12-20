package cn.ucloud.ufile.api.bucket;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.auth.BucketAuthorizer;

/**
 * Bucket相关API构造器
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/11 15:14
 */
public class BucketApiBuilder {
    protected UfileClient client;
    protected BucketAuthorizer authorizer;

    public BucketApiBuilder(UfileClient client, BucketAuthorizer authorizer) {
        this.client = client;
        this.authorizer = authorizer;
    }

    /**
     * 创建Bucket
     *
     * @param bucketName Bucket名称
     * @param region     Bucket的地区
     * @param type       Bucket类型 {@link BucketType}
     * @return {@link CreateBucketApi}
     */
    public CreateBucketApi createBucket(String bucketName, String region, BucketType type) {
        return new CreateBucketApi(authorizer, client.getHttpClient())
                .bucketName(bucketName)
                .withType(type)
                .atRegion(region);
    }

    /**
     * 删除Bucket
     *
     * @param bucketName Bucket名称
     * @return {@link DeleteBucketApi}
     */
    public DeleteBucketApi deleteBucket(String bucketName) {
        return new DeleteBucketApi(authorizer, client.getHttpClient())
                .whichBucket(bucketName);
    }

    /**
     * 更新Bucket
     *
     * @param bucketName Bucket名称
     * @param changeType 想要修改的Bucket类型 {@link BucketType}
     * @return {@link UpdateBucketApi}
     */
    public UpdateBucketApi updateBucket(String bucketName, BucketType changeType) {
        return new UpdateBucketApi(authorizer, client.getHttpClient())
                .whichBucket(bucketName)
                .changeType(changeType);
    }

    /**
     * 获取Bucket信息
     *
     * @return {@link DescribeBucketApi}
     */
    public DescribeBucketApi describeBucket() {
        return new DescribeBucketApi(authorizer, client.getHttpClient());
    }
}
