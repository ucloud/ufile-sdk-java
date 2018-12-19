package cn.ucloud.ufile.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Ufile 错误信息类
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/15 16:26
 */
public class UfileErrorBean {
    /**
     * 返回状态码, 正常返回 0
     */
    @SerializedName("RetCode")
    protected int retCode;
    /**
     * 错误消息
     */
    @SerializedName("ErrMsg")
    private String errMsg;
    /**
     * Response Header中的X-SessionId
     */
    @SerializedName("X-SessionId")
    private String xSessionId;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getxSessionId() {
        return xSessionId;
    }

    public void setxSessionId(String xSessionId) {
        this.xSessionId = xSessionId;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
