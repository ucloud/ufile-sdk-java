package cn.ucloud.ufile.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.InputStream;
import java.io.Serializable;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/15 16:52
 */
public class DownloadStreamBean implements Serializable {
    @SerializedName("ETag")
    private String eTag;
    @SerializedName("Content-Type")
    private String contentType;
    @SerializedName("Content-Length")
    private long contentLength;
    @SerializedName("InputStream")
    private InputStream inputStream;

    public String getContentType() {
        return contentType;
    }

    public DownloadStreamBean setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String geteTag() {
        return eTag;
    }

    public DownloadStreamBean seteTag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    public long getContentLength() {
        return contentLength;
    }

    public DownloadStreamBean setContentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public DownloadStreamBean setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
