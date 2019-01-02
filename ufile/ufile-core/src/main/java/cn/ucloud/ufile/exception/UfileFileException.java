package cn.ucloud.ufile.exception;


/**
 * Ufile文件异常
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/11 23:03
 */
public class UfileFileException extends UfileClientException {
    public UfileFileException(String message) {
        super(message);
    }

    public UfileFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public UfileFileException(Throwable cause) {
        super(cause);
    }
}
