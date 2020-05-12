package cn.ucloud.ufile.bean;

import cn.ucloud.ufile.bean.base.BaseResponseBean;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/21 17:30
 */
public class MultiUploadResponse extends BaseResponseBean {
    @SerializedName("Bucket")
    private String bucket;
    @SerializedName("Key")
    private String keyName;
    @SerializedName("FileSize")
    private long fileSize;
    @SerializedName("ETag")
    protected String eTag;

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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
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
