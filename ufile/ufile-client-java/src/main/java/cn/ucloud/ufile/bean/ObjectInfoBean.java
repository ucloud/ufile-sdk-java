package cn.ucloud.ufile.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/21 15:45
 */
public class ObjectInfoBean implements Serializable {
    @SerializedName("BucketName")
    private String bucketName;
    @SerializedName("CreateTime")
    private Long createTime;
    @SerializedName("FileName")
    private String fileName;
    @SerializedName("Hash")
    private String hash;
    @SerializedName("MimeType")
    private String mimeType;
    @SerializedName("ModifyTime")
    private Long modifyTime;
    @SerializedName("Size")
    private Long size;
    @SerializedName("first_object")
    private String firstObject;
    @SerializedName("StorageClass")
    private String storageClass;
    @SerializedName("RestoreStatus")
    private String restoreStatus;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFirstObject() {
        return firstObject;
    }

    public void setFirstObject(String firstObject) {
        this.firstObject = firstObject;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public String getRestoreStatus() {
        return restoreStatus;
    }

    public void setRestoreStatus(String restoreStatus) {
        this.restoreStatus = restoreStatus;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
