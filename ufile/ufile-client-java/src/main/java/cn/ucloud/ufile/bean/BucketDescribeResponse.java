package cn.ucloud.ufile.bean;

import com.google.gson.annotations.SerializedName;
import cn.ucloud.ufile.bean.base.BaseBucketResponseBean;

import java.util.List;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/28 15:25
 */
public class BucketDescribeResponse extends BaseBucketResponseBean {
    @SerializedName("DataSet")
    private List<BucketInfoBean> bucketInfoList;

    public List<BucketInfoBean> getBucketInfoList() {
        return bucketInfoList;
    }

    public void setBucketInfoList(List<BucketInfoBean> bucketInfoList) {
        this.bucketInfoList = bucketInfoList;
    }
}
