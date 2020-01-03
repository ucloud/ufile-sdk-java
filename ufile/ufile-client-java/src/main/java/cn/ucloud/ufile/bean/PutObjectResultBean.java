package cn.ucloud.ufile.bean;

import cn.ucloud.ufile.bean.base.BaseObjectResponseBean;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/15 16:52
 */
public class PutObjectResultBean extends BaseObjectResponseBean {
    @SerializedName("ETag")
    protected String eTag;

    /**
     * 上传策略-回调结果
     */
    @SerializedName("callbackRet")
    protected String callbackRet;

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
