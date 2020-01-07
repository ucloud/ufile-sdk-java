package cn.ucloud.ufile.bean;

import cn.ucloud.ufile.bean.base.BaseResponseBean;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/20 15:57
 */
public class ObjectProfile extends BaseResponseBean {
    @SerializedName("Content-Type")
    private String contentType;
    @SerializedName("Content-Length")
    private long contentLength;
    @SerializedName("ETag")
    private String eTag;
    @SerializedName("Accept-Ranges")
    private String acceptRanges;
    @SerializedName("X-Ufile-Create-Time")
    private String createTime;
    @SerializedName("Last-Modified")
    private String lastModified;
    @SerializedName("Vary")
    private String vary;
    @SerializedName("Bucket")
    private String bucket;
    @SerializedName("KeyName")
    private  String keyName;
    private transient Map<String,String> metadatas;
    /**
     * 请求下载文件的存储类型，分别是标准、低频、冷存，对应有效值：STANDARD | IA | ARCHIVE
     */
    @SerializedName("StorageType")
    private String storageType;
    /**
     * 如果请求下载文件的存储类型为ARCHIVE，且文件处于解冻状态，则这个响应头会返回对应文件的解冻过期时间
     */
    @SerializedName("RestoreTime")
    private String restoreTime;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getAcceptRanges() {
        return acceptRanges;
    }

    public void setAcceptRanges(String acceptRanges) {
        this.acceptRanges = acceptRanges;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getVary() {
        return vary;
    }

    public void setVary(String vary) {
        this.vary = vary;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public Map<String,String> getMetadatas() {
        return metadatas;
    }

    public void setMetadatas(Map<String,String> metadatas) {
        this.metadatas = metadatas;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getRestoreTime() {
        return restoreTime;
    }

    public void setRestoreTime(String restoreTime) {
        this.restoreTime = restoreTime;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
