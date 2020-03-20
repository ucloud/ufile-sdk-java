package cn.ucloud.ufile.util;

/**
 * @description:
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2020/3/20 21:26
 */
public enum UfileProtocol {
    PROTOCOL_HTTP("http://"),
    PROTOCOL_HTTPS("https://");

    private String value;

    UfileProtocol(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
