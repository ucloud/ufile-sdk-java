package cn.ucloud.ufile.auth;

/**
 * 授权者
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 16:27
 */
public interface Authorizer {

    /**
     * 获取用户公钥
     *
     * @return 用户公钥
     */
    String getPublicKey();
}
