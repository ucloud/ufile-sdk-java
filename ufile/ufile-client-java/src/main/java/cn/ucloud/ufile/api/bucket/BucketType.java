package cn.ucloud.ufile.api.bucket;

/**
 * Bucket类型(PUBLIC or PRIVATE)
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/10 16:16
 */
public enum BucketType {
    /**
     * 公共的Bucket类型
     */
    PUBLIC("public"),
    /**
     * 私有的Bucket类型
     */
    PRIVATE("private");

    private String bucketType;

    BucketType(String bucketType) {
        this.bucketType = bucketType;
    }

    /**
     * 获取Bucket类型
     * @return Bucket类型
     */
    public String getBucketType() {
        return bucketType;
    }
}
