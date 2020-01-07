package cn.ucloud.ufile.bean;

import cn.ucloud.ufile.bean.base.BaseResponseBean;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/15 16:52
 */
public class DownloadStreamBean extends BaseResponseBean {
    @SerializedName("ETag")
    private String eTag;
    @SerializedName("Content-Type")
    private String contentType;
    @SerializedName("Content-Length")
    private long contentLength;
    @SerializedName("InputStream")
    private InputStream inputStream;

    private transient Map<String,String> metadatas;

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

    public Map<String,String> getMetadatas() {
        return metadatas;
    }

    public void setMetadatas(Map<String,String> metadatas) {
        this.metadatas = metadatas;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
