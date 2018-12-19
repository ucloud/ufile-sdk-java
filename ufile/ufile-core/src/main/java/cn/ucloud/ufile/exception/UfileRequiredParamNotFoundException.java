package cn.ucloud.ufile.exception;


/**
 * Ufile必要参数未找到异常
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/13 16:25
 */
public class UfileRequiredParamNotFoundException extends UfileException {
    public UfileRequiredParamNotFoundException(String message) {
        super(message);
    }

    public UfileRequiredParamNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UfileRequiredParamNotFoundException(Throwable cause) {
        super(cause);
    }
}
