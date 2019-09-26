package cn.ucloud.ufile.bean;

import com.google.gson.annotations.SerializedName;


/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/15 16:52
 */
public class CopyObjectResultBean extends PutObjectResultBean {
    @SerializedName("LastModified")
    private long lastModified;

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}
