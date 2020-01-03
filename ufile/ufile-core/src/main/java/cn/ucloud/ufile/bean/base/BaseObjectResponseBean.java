package cn.ucloud.ufile.bean.base;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Response 基础类
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/15 16:26
 */
public class BaseObjectResponseBean extends BaseResponseBean {
    /**
     * 返回状态码, 正常返回 0
     */
    @SerializedName("RetCode")
    protected int retCode;
    /**
     * 返回消息，部分API可能为空
     */
    @SerializedName("Message")
    protected String message;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
