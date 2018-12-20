package cn.ucloud.ufile.auth;

import cn.ucloud.ufile.auth.sign.UfileSignatureException;
import cn.ucloud.ufile.util.HttpMethod;

/**
 * 对象存储相关API授权者
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 16:27
 */
public interface ObjectAuthorizer extends Authorizer {

    /**
     * 获取授权签名
     *
     * @param param 授权参数 {@link ObjectOptAuthParam}
     * @return 授权签名
     * @throws UfileAuthorizationException 授权异常时抛出
     * @throws UfileSignatureException     签名异常时抛出
     */
    String authorization(ObjectOptAuthParam param)
            throws UfileAuthorizationException, UfileSignatureException;

    /**
     * 获取私有仓库URL签名
     *
     * @param param 授权参数 {@link ObjectDownloadAuthParam}
     * @return 私有仓库URL签名
     * @throws UfileAuthorizationException 授权异常时抛出
     * @throws UfileSignatureException     签名异常时抛出
     */
    String authorizePrivateUrl(ObjectDownloadAuthParam param)
            throws UfileAuthorizationException, UfileSignatureException;
}
