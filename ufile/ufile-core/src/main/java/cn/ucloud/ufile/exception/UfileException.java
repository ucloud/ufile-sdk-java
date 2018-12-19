package cn.ucloud.ufile.exception;

/**
 * Ufile异常
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/7 18:22
 */
public class UfileException extends Exception {

    public UfileException() {
    }

    public UfileException(String message) {
        super(message);
    }

    public UfileException(String message, Throwable cause) {
        super(message, cause);
    }

    public UfileException(Throwable cause) {
        super(cause);
    }

    public UfileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
