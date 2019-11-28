package cn.ucloud.ufile.bean;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/21 15:45
 */
public class ObjectContentBean implements Serializable {
    @SerializedName("BucketName")
    private String bucketName;
    @SerializedName("Key")
    private String key;
    @SerializedName("MimeType")
    private String mimeType;
    @SerializedName("Etag")
    private String eTag;
    @SerializedName("Size")
    private String size;
    @SerializedName("StorageClass")
    private String storageType;
    @SerializedName("LastModified")
    private Long lastModified;
    @SerializedName("CreateTime")
    private Long createTime;
    @SerializedName("UserMeta")
    private JsonObject jsonUserMeta;
    private transient Map<String, String> userMeta;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public JsonObject getJsonUserMeta() {
        return jsonUserMeta;
    }

    public Map<String, String> getUserMeta() {
        return userMeta;
    }

    public void setUserMeta(Map<String, String> userMeta) {
        this.userMeta = userMeta;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
