package cn.ucloud.ufile.exception;

import cn.ucloud.ufile.bean.UfileErrorBean;

/**
 * Ufile异常
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/7 18:22
 */
public class UfileServerException extends Exception {
    private UfileErrorBean errorBean;

    public UfileServerException(UfileErrorBean errorBean) {
        super(errorBean == null ? "" : errorBean.toString());
        this.errorBean = errorBean;
    }

    public UfileServerException(String message, UfileErrorBean errorBean) {
        super(message);
        this.errorBean = errorBean;
    }

    public UfileServerException(String message) {
        super(message);
    }

    public UfileServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public UfileServerException(Throwable cause) {
        super(cause);
    }

    public UfileErrorBean getErrorBean() {
        return errorBean;
    }
}
