package cn.ucloud.ufile.http;

import cn.ucloud.ufile.exception.UfileException;

/**
 * UfileHttp异常
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/13 22:30
 */
public class UfileHttpException extends UfileException {
    public UfileHttpException(String message) {
        super(message);
    }

    public UfileHttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public UfileHttpException(Throwable cause) {
        super(cause);
    }
}
