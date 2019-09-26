package cn.ucloud.ufile.api.object.policy;

import cn.ucloud.ufile.util.Parameter;

/**
 * @description:
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2019/9/19 14:48
 */
public class PolicyParam extends Parameter<String> {

    public PolicyParam(String key, String value) {
        super(key, value);
    }

    protected String format() {
        if (key == null || key.isEmpty())
            return null;

        return new StringBuilder(key).append("=").append(value == null ? "" : value).toString();
    }
}
