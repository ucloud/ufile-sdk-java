package cn.ucloud.ufile.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/21 17:30
 */
public class MultiUploadResponse implements Serializable {
    @SerializedName("Bucket")
    private String bucket;
    @SerializedName("Key")
    private String keyName;
    @SerializedName("FileSize")
    private int fileSize;

    /**
     * 上传策略-回调结果
     */
    @SerializedName("callbackRet")
    protected String callbackRet;

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

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getCallbackRet() {
        return callbackRet;
    }

    public void setCallbackRet(String callbackRet) {
        this.callbackRet = callbackRet;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
