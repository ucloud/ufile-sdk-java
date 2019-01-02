package cn.ucloud.ufile.exception;


/**
 * Ufile IO操作异常
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/11 23:03
 */
public class UfileIOException extends UfileClientException {
    public UfileIOException(String message) {
        super(message);
    }

    public UfileIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public UfileIOException(Throwable cause) {
        super(cause);
    }
}
