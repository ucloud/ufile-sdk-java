package cn.ucloud.ufile.auth.sign;


/**
 *  签名器
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/7 17:00
 */
public interface Signer {
    /**
     * 计算签名
     *
     * @param key  签名Key
     * @param data 签名数据
     * @return 签名结果
     * @throws UfileSignatureException
     */
    String signature(String key, String data) throws UfileSignatureException;

    /**
     * 计算Bucket URL签名
     *
     * @param data 签名数据
     * @return 签名结果
     * @throws UfileSignatureException
     */
    String signatureBucket(String data) throws UfileSignatureException;
}
