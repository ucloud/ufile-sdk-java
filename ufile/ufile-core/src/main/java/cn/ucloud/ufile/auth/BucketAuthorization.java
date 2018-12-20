package cn.ucloud.ufile.auth;


/**
 * 授权者
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/6 18:50
 */
public abstract class BucketAuthorization implements BucketAuthorizer {
    /**
     * 用户公钥
     */
    protected String publicKey;

    /**
     * 构造方法
     *
     * @param publicKey 用户公钥
     */
    public BucketAuthorization(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }
}
