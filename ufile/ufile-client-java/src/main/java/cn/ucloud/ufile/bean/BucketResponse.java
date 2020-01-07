package cn.ucloud.ufile.bean;

import com.google.gson.annotations.SerializedName;
import cn.ucloud.ufile.bean.base.BaseBucketResponseBean;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/15 16:52
 */
public class BucketResponse extends BaseBucketResponseBean {
    @SerializedName("BucketName")
    private String bucketName;

    @SerializedName("BucketId")
    private String bucketId;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketId() {
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }
}
