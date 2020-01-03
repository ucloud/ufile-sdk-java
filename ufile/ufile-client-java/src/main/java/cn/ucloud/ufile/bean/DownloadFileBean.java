package cn.ucloud.ufile.bean;

import cn.ucloud.ufile.bean.base.BaseResponseBean;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.Map;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/15 16:52
 */
public class DownloadFileBean extends BaseResponseBean {
    @SerializedName("ETag")
    private String eTag;
    @SerializedName("Content-Type")
    private String contentType;
    @SerializedName("Content-Length")
    private long contentLength;
    @SerializedName("File")
    private File file;

    private transient Map<String,String> metadatas;

    public String getContentType() {
        return contentType;
    }

    public DownloadFileBean setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String geteTag() {
        return eTag;
    }

    public DownloadFileBean seteTag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    public long getContentLength() {
        return contentLength;
    }

    public DownloadFileBean setContentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public File getFile() {
        return file;
    }

    public DownloadFileBean setFile(File file) {
        this.file = file;
        return this;
    }

    public Map<String,String> getMetadatas() {
        return metadatas;
    }

    public DownloadFileBean setMetadatas(Map<String,String> metadatas) {
        this.metadatas = metadatas;
        return this;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
