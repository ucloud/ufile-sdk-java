package cn.ucloud.ufile.api.object.multi;

import cn.ucloud.ufile.bean.MultiUploadPartInfoBean;
import cn.ucloud.ufile.bean.base.BaseResponseBean;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/*
    UploadId	string	上传ID
    Bucket	string	空间名称
    Key	string	文件名称
    Parts	array	分片列表
    MaxParts	Integer	返回请求中最大的Part数目。
    IsTruncated	Bool	标明本次返回的ListParts结果列表是否被截断。
    Parts	Array	分片信息。
    NextPartNumberMarker	Integer	如果本次没有返回全部结果，响应请求中将包含NextPartNumberMarker元素，用于标明接下来请求的PartNumberMarker值。

 */
public class MultiUploadListPartsInfo extends BaseResponseBean {
    /**
     * UploadId
     */
    @SerializedName("UploadId")
    private String uploadId;
    /**
     * 返回请求中最大的Part数目。
     */
    @SerializedName("MaxParts")
    private long maxParts;
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
     * Parts
     */
    @SerializedName("Parts")
    private List<MultiUploadPartInfoBean> parts;
    /**
     * Parts
     */
    @SerializedName("IsTruncated")
    private boolean isTruncated;

    @SerializedName("NextPartNumberMarker")
    private long nextPartNumberMarker;

    public String getUploadId() {
        return uploadId;
    }

    public long getNextPartNumberMarker() {
        return nextPartNumberMarker;
    }

    public String getBucket() {
        return bucket;
    }

    public String getKeyName() {
        return keyName;
    }

    public long getMaxParts() {
        return maxParts;
    }

    public boolean getIsTruncated() {
        return this.isTruncated = isTruncated;
    }

    public List<MultiUploadPartInfoBean> getParts(){
        return this.parts;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
