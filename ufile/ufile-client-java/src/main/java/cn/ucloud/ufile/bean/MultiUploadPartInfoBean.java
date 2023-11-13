package cn.ucloud.ufile.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MultiUploadPartInfoBean implements Serializable {

    @SerializedName("PartNum")
    private Long partNum;
    @SerializedName("Size")
    private Long size;
    @SerializedName("Etag")
    private String etag;
    @SerializedName("LastModified")
    private Long lastModified;


    public Long getPartNum() {
        return size;
    }

    public void setPartNum(long partNum) {
        this.partNum = partNum;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }


    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}