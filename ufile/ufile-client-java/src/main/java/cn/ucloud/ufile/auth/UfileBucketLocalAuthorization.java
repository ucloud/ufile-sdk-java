package cn.ucloud.ufile.auth;

import cn.ucloud.ufile.auth.sign.Signer;
import cn.ucloud.ufile.auth.sign.UfileSignatureException;
import cn.ucloud.ufile.auth.sign.UfileSigner;
import cn.ucloud.ufile.util.Parameter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *   Ufile默认的本地签名生成器
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/7 15:31
 */
public final class UfileBucketLocalAuthorization extends BucketLocalAuthorization {
    /**
     * 构造方法 (使用UFile默认签名器 {@link UfileSigner})
     *
     * @param publicKey  用户公钥
     * @param privateKey 用户私钥
     */
    public UfileBucketLocalAuthorization(String publicKey, String privateKey) {
        super(publicKey, privateKey);
    }

    /**
     * 构造方法 (若您的运行环境在Java 1.8以下，请使用该方法)
     *
     * @param publicKey  用户公钥
     * @param privateKey 用户私钥
     * @param signer     签名器 {@link Signer}
     */
    public UfileBucketLocalAuthorization(String publicKey, String privateKey, Signer signer) {
        super(publicKey, privateKey, signer);
    }

    @Override
    public String authorizeBucketUrl(List<Parameter<String>> urlQuery)
            throws UfileAuthorizationException, UfileSignatureException {
        if (urlQuery == null)
            throw new UfileAuthorizationException("Param 'urlQuery' can not be null!");

        Collections.sort(urlQuery, keyComparator);
        StringBuffer signData = new StringBuffer();
        for (Parameter<String> param : urlQuery) {
            signData.append(param.key + param.value);
        }

        signData.append(privateKey);

        return signer.signatureBucket(signData.toString());
    }

    /**
     * key排序比较器
     */
    private Comparator<Parameter> keyComparator = new Comparator<Parameter>() {
        @Override
        public int compare(Parameter o1, Parameter o2) {
            return o1.key.compareTo(o2.key);
        }
    };

}
