package cn.ucloud.ufile.auth.sign;

import cn.ucloud.ufile.exception.UfileException;

/**
 * 签名异常
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/7 18:10
 */
public class UfileSignatureException extends UfileException {
    public UfileSignatureException(String message) {
        super(message);
    }

    public UfileSignatureException(String message, Throwable cause) {
        super(message, cause);
    }

    public UfileSignatureException(Throwable cause) {
        super(cause);
    }
}
