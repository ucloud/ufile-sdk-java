package cn.ucloud.ufile.bean;

import cn.ucloud.ufile.bean.base.BaseResponseBean;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/21 15:41
 */
public class ObjectListWithDirFormatBean extends BaseResponseBean {
    @SerializedName("Name")
    private String bucketName;
    @SerializedName("Prefix")
    private String prefix;
    @SerializedName("Maxkeys")
    private String maxKeys;
    @SerializedName("Delimiter")
    private String delimiter;
    @SerializedName("IsTruncated")
    private Boolean isTruncated;
    @SerializedName("NextMarker")
    private String nextMarker;
    @SerializedName("Contents")
    private List<ObjectContentBean> objectList;
    @SerializedName("CommonPrefixes")
    private List<CommonPrefix> commonPrefixes;

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

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getMaxKeys() {
        return maxKeys;
    }

    public void setMaxKeys(String maxKeys) {
        this.maxKeys = maxKeys;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public Boolean getTruncated() {
        return isTruncated;
    }

    public void setTruncated(Boolean truncated) {
        isTruncated = truncated;
    }

    public List<ObjectContentBean> getObjectContents() {
        return objectList;
    }

    public void setObjectContents(List<ObjectContentBean> objectList) {
        this.objectList = objectList;
    }

    public List<CommonPrefix> getCommonPrefixes() {
        return commonPrefixes;
    }

    public void setCommonPrefixes(List<CommonPrefix> commonPrefixes) {
        this.commonPrefixes = commonPrefixes;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
