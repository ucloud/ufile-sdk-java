package cn.ucloud.ufile.auth;

import cn.ucloud.ufile.auth.sign.Signer;
import cn.ucloud.ufile.auth.sign.UfileSignatureException;
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

    public UfileBucketLocalAuthorization(String publicKey, String privateKey) {
        super(publicKey, privateKey);
    }

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
    private Comparator<Parameter> keyComparator = Comparator.comparing(o -> o.key);

}
