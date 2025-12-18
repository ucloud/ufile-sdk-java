package cn.ucloud.ufile.auth;

import cn.ucloud.ufile.auth.sign.Signer;
import cn.ucloud.ufile.auth.sign.UfileSignatureException;
import cn.ucloud.ufile.auth.sign.UfileSigner;
import cn.ucloud.ufile.util.HttpMethod;
import cn.ucloud.ufile.util.JLog;

import java.util.Objects;

/**
 * Ufile默认的本地签名生成器
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/7 15:31
 */
public final class UfileObjectLocalAuthorization extends ObjectLocalAuthorization {

    /**
     * 构造方法 (使用UFile默认签名器 {@link UfileSigner})
     *
     * @param publicKey  用户公钥
     * @param privateKey 用户私钥
     */
    public UfileObjectLocalAuthorization(String publicKey, String privateKey) {
        super(publicKey, privateKey);
    }


    /**
     * 构造方法 (若您的运行环境在Java 1.8以下，请使用该方法)
     *
     * @param publicKey  用户公钥
     * @param privateKey 用户私钥
     * @param signer     签名器 {@link Signer}
     */
    public UfileObjectLocalAuthorization(String publicKey, String privateKey, Signer signer) {
        super(publicKey, privateKey, signer);
    }

    @Override
    public String authorization(ObjectOptAuthParam param) throws UfileAuthorizationException, UfileSignatureException {
        if (param == null)
            throw new UfileAuthorizationException("Param can not be null!");

        if (privateKey.isEmpty() || publicKey.isEmpty()){
            return "";
        }

        HttpMethod method = param.getMethod();
        String bucket = param.getBucket();
        String keyName = param.getKeyName();
        String contentType = param.getContentType();
        String contentMD5 = param.getContentMD5();
        String date = param.getDate();
        String xUFileCopySource = param.getXUFileCopySource();
        String xUFileCopySourceRange = param.getXUFileCopySourceRange();

        if (method == null)
            throw new UfileAuthorizationException("Param 'method' can not be null!");

        if (param.getBucket() == null || param.getBucket().length() == 0)
            throw new UfileAuthorizationException("Param 'bucket' can not be blank!");

        keyName = keyName == null ? "" : keyName;
        contentType = contentType == null ? "" : contentType;
        contentMD5 = contentMD5 == null ? "" : contentMD5;
        date = date == null ? "" : date;
        xUFileCopySource = xUFileCopySource == null ? "" : "x-ufile-copy-source:" + xUFileCopySource + "\n";
        xUFileCopySourceRange = xUFileCopySourceRange == null ? "" : "x-ufile-copy-source-range:" + xUFileCopySourceRange + "\n";

        StringBuffer signData = new StringBuffer();
        signData.append(method.getName() + "\n");
        signData.append(contentMD5 + "\n");
        signData.append(contentType + "\n");
        signData.append(date + "\n");
        signData.append(xUFileCopySource);
        signData.append(xUFileCopySourceRange);
        signData.append("/" + bucket);
        signData.append("/" + keyName);
        if (param.getPutPolicy() != null && param.getPutPolicy().getPolicy() != null)
            signData.append(param.getPutPolicy().getPolicy());

        JLog.D("TEST", "[signData]:" + signData.toString());

        String signature = signer.signature(privateKey, signData.toString());

        return "UCloud " + publicKey + ":" + signature
                + ((param.getPutPolicy() == null || param.getPutPolicy().getPolicy() == null) ?
                "" : (":" + param.getPutPolicy().getPolicy()));
    }

    @Override
    public String authorizePrivateUrl(ObjectDownloadAuthParam param) throws UfileAuthorizationException, UfileSignatureException {
        if (param == null)
            throw new UfileAuthorizationException("Param can not be null!");

        HttpMethod method = param.getMethod();
        String bucket = param.getBucket();
        String keyName = param.getKeyName();
        long expires = param.getExpires();

        if (method == null)
            throw new UfileAuthorizationException("Param 'method' can not be null!");
        if (bucket == null || bucket.length() == 0)
            throw new UfileAuthorizationException("Param 'bucket' can not be blank!");
        if (keyName == null || keyName.length() == 0)
            throw new UfileAuthorizationException("Param 'which' can not be blank!");
        if (expires <= 0)
            throw new UfileAuthorizationException("Param 'expires' must be > 0!");

        String contentMD5 = param.getContentMD5();
        String contentType = param.getContentType();
        contentMD5 = contentMD5 == null ? "" : contentMD5;
        contentType = contentType == null ? "" : contentType;

        StringBuffer signData = new StringBuffer();
        signData.append(method.getName() + "\n");
        signData.append(contentMD5 + "\n");
        signData.append(contentType + "\n");
        signData.append(expires + "\n");
        signData.append("/" + bucket);
        signData.append("/" + keyName);

        String signature = signer.signature(privateKey, signData.toString());

        return signature;
    }
}
