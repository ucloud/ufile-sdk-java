package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;

/**
 * API-生成公共下载URL
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/16 12:00
 */
public class GenerateObjectPublicUrlApi {
    private String host;
    /**
     * Required
     * 云端对象名称
     */
    private String keyName;
    /**
     * Required
     * Bucket空间名称
     */
    private String bucketName;

    /**
     * 构造方法
     *
     * @param host       API域名
     * @param keyName    对象名称
     * @param bucketName bucket名称
     */
    protected GenerateObjectPublicUrlApi(String host, String keyName, String bucketName) {
        this.host = host;
        this.keyName = keyName;
        this.bucketName = bucketName;
    }

    /**
     * 生成下载URL
     *
     * @return 下载URL
     * @throws UfileRequiredParamNotFoundException 必要参数未找到时抛出
     */
    public String createUrl() throws UfileParamException {
        parameterValidat();
        return generateFinalHost(bucketName, keyName);
    }

    private String generateFinalHost(String bucketName, String keyName) {
        if (host == null || host.length() == 0)
            return host;

        if (host.startsWith("http"))
            return String.format("%s/%s", host, keyName);

        return String.format("http://%s.%s/%s", bucketName, host, keyName);
    }

    protected void parameterValidat() throws UfileParamException {
        if (keyName == null || keyName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'keyName' can not be null or empty");

        if (bucketName == null || bucketName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'bucketName' can not be null or empty");
    }
}
