package cn.ucloud.ufile.bean.base;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

/**
 * Response 基础类
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/15 16:26
 */
public class BaseResponseBean implements Serializable {
    protected transient Map<String, String> headers;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        if (headers != null) {
            String eTag = headers.get("ETag");
            if (eTag != null) {
                headers.put("ETag", eTag.replace("\"", ""));
            }
        }

        this.headers = headers;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
