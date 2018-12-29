package cn.ucloud.ufile.api;

/**
 * API错误体
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/15 17:13
 */
public class ApiError {
    public enum ErrorType {
        /**
         * 一般错误，如计算过程、线程中断等
         */
        ERROR_NORMAL_ERROR,
        /**
         * 网络异常
         */
        ERROR_NETWORK_ERROR,
        /**
         * HTTP错误，Response Code为非正常值
         */
        ERROR_SERVER_ERROR,
        /**
         * Http response为空
         */
        ERROR_RESPONSE_IS_NULL,
        /**
         * Http response解析失败
         */
        ERROR_RESPONSE_SPARSE_FAILED,
        /**
         * 参数非法或无效
         */
        ERROR_PARAMS_ILLEGAL,
    }

    private ErrorType type;
    private String message;
    private Throwable throwable;
    private int responseCode = -1;

    /**
     * 构造方法
     *
     * @param type 错误类型
     */
    public ApiError(ErrorType type) {
        this.type = type;
        message = type.name();
    }

    /**
     * 构造方法
     *
     * @param type    错误类型
     * @param message 信息
     */
    public ApiError(ErrorType type, String message) {
        this.type = type;
        this.message = message;
    }

    /**
     * 构造方法
     *
     * @param type      错误类型
     * @param throwable 抛出的异常
     */
    public ApiError(ErrorType type, Throwable throwable) {
        this.type = type;
        this.message = throwable.getMessage();
        this.throwable = throwable;
    }

    /**
     * 构造方法
     *
     * @param type      错误类型
     * @param message   信息
     * @param throwable 抛出的异常
     */
    public ApiError(ErrorType type, String message, Throwable throwable) {
        this.type = type;
        this.message = message;
        this.throwable = throwable;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public ErrorType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public ApiError setType(ErrorType type) {
        this.type = type;
        return this;
    }

    public ApiError setMessage(String message) {
        this.message = message;
        return this;
    }

    public ApiError setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public ApiError setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName() + "--->");
        sb.append(String.format(" [type]: %s", type));
        sb.append(String.format(" [message]: %s", message));
        sb.append(String.format(" [responseCode]: %s", responseCode));
        sb.append(String.format(" [throwable]: %s", throwable));

        return sb.toString();
    }
}
