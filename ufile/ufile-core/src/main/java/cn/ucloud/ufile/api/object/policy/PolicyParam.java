package cn.ucloud.ufile.api.object.policy;

import cn.ucloud.ufile.util.Param;

/**
 * @description:
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2019/9/19 14:48
 */
public class PolicyParam extends Param<String, String> {

    public PolicyParam(String key, String value) {
        super(key, value);
    }

    @Override
    protected String format() {
        if (key == null || key.isEmpty())
            return null;

        if (value == null)
            value = "";

        return new StringBuilder(key).append("=").append(value).toString();
    }
}
