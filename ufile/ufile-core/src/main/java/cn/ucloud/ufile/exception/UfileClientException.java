package cn.ucloud.ufile.exception;

/**
 * Ufile异常
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/7 18:22
 */
public class UfileClientException extends Exception {

    public UfileClientException() {
    }

    public UfileClientException(String message) {
        super(message);
    }

    public UfileClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public UfileClientException(Throwable cause) {
        super(cause);
    }

    public UfileClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
