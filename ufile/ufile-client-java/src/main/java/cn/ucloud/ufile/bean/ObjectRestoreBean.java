package cn.ucloud.ufile.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * API-获取云端对象描述信息
 *
 * @author: delex
 * @E-mail: delex.xie@ucloud.cn
 * @date: 2019/08/27 19:09
 */
public class ObjectRestoreBean implements Serializable {
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
