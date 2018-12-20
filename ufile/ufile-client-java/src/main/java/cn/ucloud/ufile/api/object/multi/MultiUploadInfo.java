package cn.ucloud.ufile.api.object.multi;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * 分片上传初始化信息
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/21 17:30
 */
public class MultiUploadInfo {
    /**
     * UploadId
     */
    @SerializedName("UploadId")
    private String uploadId;
    /**
     * 分片大小
     */
    @SerializedName("BlkSize")
    private int blkSize;
    /**
     * 目标Bucket
     */
    @SerializedName("Bucket")
    private String bucket;
    /**
     * 云端对象名称
     */
    @SerializedName("Key")
    private String keyName;
    /**
     * 对象mimeType
     */
    @SerializedName("MimeType")
    private String mimeType;

    public String getUploadId() {
        return uploadId;
    }

    public int getBlkSize() {
        return blkSize;
    }

    public String getBucket() {
        return bucket;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getMimeType() {
        return mimeType;
    }

    protected void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
