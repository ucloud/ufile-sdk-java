package cn.ucloud.ufile.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/21 17:30
 */
public class MultiUploadResponse {
    @SerializedName("Bucket")
    private String bucket;
    @SerializedName("Key")
    private String keyName;
    @SerializedName("FileSize")
    private int fileSize;

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

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
