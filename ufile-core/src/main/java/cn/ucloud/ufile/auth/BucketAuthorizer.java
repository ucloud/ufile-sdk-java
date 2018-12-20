package cn.ucloud.ufile.auth;

import cn.ucloud.ufile.auth.sign.UfileSignatureException;
import cn.ucloud.ufile.util.Parameter;

import java.util.List;

/**
 * Bucket相关API授权者
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 16:27
 */
public interface BucketAuthorizer extends Authorizer{

    /**
     * 获取Bucket URL签名
     *
     * @param urlQuery Http GET请求的Query参数
     * @return Bucket URL签名
     * @throws UfileAuthorizationException
     * @throws UfileSignatureException
     */
    String authorizeBucketUrl(List<Parameter<String>> urlQuery) throws UfileAuthorizationException, UfileSignatureException;
}
