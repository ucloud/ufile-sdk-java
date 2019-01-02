package cn.ucloud.ufile.exception;


/**
 * Ufile参数异常
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/13 16:25
 */
public class UfileParamException extends UfileClientException {
    public UfileParamException(String message) {
        super(message);
    }

    public UfileParamException(String message, Throwable cause) {
        super(message, cause);
    }

    public UfileParamException(Throwable cause) {
        super(cause);
    }
}
