package cn.ucloud.ufile.bean;

import cn.ucloud.ufile.bean.base.BaseResponseBean;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/21 15:41
 */
public class ObjectListBean extends BaseResponseBean {
    @SerializedName("BucketId")
    private String bucketId;
    @SerializedName("BucketName")
    private String bucketName;
    @SerializedName("NextMarker")
    private String nextMarker;
    @SerializedName("DataSet")
    private List<ObjectInfoBean> objectList;

    public String getBucketId() {
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getNextMarker() {
        return nextMarker;
    }

    public void setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
    }

    public List<ObjectInfoBean> getObjectList() {
        return objectList;
    }

    public void setObjectList(List<ObjectInfoBean> objectList) {
        this.objectList = objectList;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
