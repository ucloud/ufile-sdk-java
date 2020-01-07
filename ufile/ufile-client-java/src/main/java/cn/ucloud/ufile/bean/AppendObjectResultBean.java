package cn.ucloud.ufile.bean;

import cn.ucloud.ufile.bean.base.BaseObjectResponseBean;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;


/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/15 16:52
 */
public class AppendObjectResultBean extends BaseObjectResponseBean {
    @SerializedName("ETag")
    private String eTag;

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
