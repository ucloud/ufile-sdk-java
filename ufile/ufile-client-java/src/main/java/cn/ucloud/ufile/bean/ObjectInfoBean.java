package cn.ucloud.ufile.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/21 15:45
 */
public class ObjectInfoBean implements Serializable {
    @SerializedName("BucketName")
    private String bucketName;
    @SerializedName("CreateTime")
    private long createTime;
    @SerializedName("FileName")
    private String fileName;
    @SerializedName("Hash")
    private String hash;
    @SerializedName("MimeType")
    private String mimeType;
    @SerializedName("ModifyTime")
    private long modifyTime;
    @SerializedName("Size")
    private long size;
    @SerializedName("first_object")
    private String firstObject;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public long getCreateTime() {
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

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public long getSize() {
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

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
