package cn.ucloud.ufile.auth;

import cn.ucloud.ufile.exception.UfileException;

/**
 * 授权异常
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/7 18:19
 */
public class UfileAuthorizationException extends UfileException {
    public UfileAuthorizationException(String message) {
        super(message);
    }

    public UfileAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UfileAuthorizationException(Throwable cause) {
        super(cause);
    }
}
