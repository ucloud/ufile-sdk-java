package cn.ucloud.ufile.http.response;

import okhttp3.Response;

/**
 * Http Response 解析器
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/15 14:32
 */
public interface ResponseParser<R, E> {
    /**
     * 解析Http Response
     *
     * @param response 源Response
     * @return 泛型指定返回类型
     * @throws Throwable
     */
    R parseHttpResponse(Response response) throws Throwable;

    /**
     * 解析Http Error Response
     *
     * @param response 源Response
     * @return 泛型指定返回类型
     * @throws Throwable
     */
    E parseErrorResponse(Response response) throws Throwable;
}
