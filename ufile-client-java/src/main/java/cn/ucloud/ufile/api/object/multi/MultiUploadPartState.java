package cn.ucloud.ufile.api.object.multi;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import cn.ucloud.ufile.bean.base.BaseResponseBean;

/**
 * 上传分片数据状态
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/15 16:52
 */
public class MultiUploadPartState extends BaseResponseBean {
    /**
     * ETag
     */
    @SerializedName("ETag")
    private String eTag;
    /**
     * 分片序号
     */
    @SerializedName("PartNumber")
    private int partIndex = -1;

    public String geteTag() {
        return eTag;
    }

    protected void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public int getPartIndex() {
        return partIndex;
    }

    protected void setPartIndex(int partIndex) {
        this.partIndex = partIndex;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
