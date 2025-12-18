package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.request.GetRequestBuilder;
import cn.ucloud.ufile.util.Encoder;
import cn.ucloud.ufile.util.Parameter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * API-生成公共下载URL
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/16 12:00
 */
public class GenerateObjectPublicUrlApi {
    private ObjectConfig objectConfig;
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
     * Ufile 文件下载链接所需的Content-Disposition: attachment文件名
     */
    private String attachmentFileName;

    /**
     * 图片处理服务
     */
    protected String iopCmd;

    /**
     * 构造方法
     *
     * @param objectConfig ObjectConfig {@link ObjectConfig}
     * @param keyName      对象名称
     * @param bucketName   bucket名称
     */
    protected GenerateObjectPublicUrlApi(ObjectConfig objectConfig, String keyName, String bucketName) {
        this.objectConfig = objectConfig;
        this.keyName = keyName;
        this.bucketName = bucketName;
    }

    /**
     * 使用Content-Disposition: attachment，并配置attachment的文件名
     *
     * @param attachmentFileName Content-Disposition: attachment的文件名
     * @return
     */
    public GenerateObjectPublicUrlApi withAttachment(String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
        return this;
    }

    /**
     * 使用Content-Disposition: attachment，并且文件名默认为keyName
     *
     * @return
     */
    public GenerateObjectPublicUrlApi withAttachment() {
        this.attachmentFileName = keyName;
        return this;
    }

    /**
     * 针对图片文件的操作参数
     *
     * @param iopCmd 请参考 https://docs.ucloud.cn/ufile/service/pic
     * @return {@link GenerateObjectPublicUrlApi}
     */
    public GenerateObjectPublicUrlApi withIopCmd(String iopCmd) {
        this.iopCmd = iopCmd;
        return this;
    }

    /**
     * 生成下载URL
     *
     * @return 下载URL
     * @throws UfileRequiredParamNotFoundException 必要参数未找到时抛出
     */
    public String createUrl() throws UfileClientException {
        parameterValidat();
        GetRequestBuilder builder = (GetRequestBuilder) new GetRequestBuilder()
                .baseUrl(generateFinalHost(bucketName, keyName));

        if (attachmentFileName != null && !attachmentFileName.isEmpty()) {
            try {
                attachmentFileName = Encoder.urlEncode(attachmentFileName, "UTF-8");
                builder.addParam(new Parameter("ufileattname", attachmentFileName));
            } catch (UnsupportedEncodingException e) {
                throw new UfileClientException("Occur error during URLEncode attachmentFileName", e);
            }
        }

        String url = builder.generateGetUrl(builder.getBaseUrl(), builder.getParams());
        if (iopCmd != null && !iopCmd.isEmpty()) {
            List<Parameter<String>> params = builder.getParams();
            if (params == null || params.isEmpty()) {
                url = String.format("%s?%s", url, iopCmd);
            } else {
                url = String.format("%s&%s", url, iopCmd);
            }
        }

        return url;
    }

    private String generateFinalHost(String bucketName, String keyName) throws UfileClientException {
        if (objectConfig == null)
            return null;

        if (objectConfig.isCustomDomain()) {
            try {
                keyName = Encoder.urlEncodePath(keyName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new UfileClientException("Occur error during URLEncode keyName", e);
            }
            return String.format("%s/%s", objectConfig.getCustomHost(), keyName);
        }

        try {
            bucketName = Encoder.urlEncode(bucketName, "UTF-8");
            String region = Encoder.urlEncode(objectConfig.getRegion(), "UTF-8");
            String proxySuffix = Encoder.urlEncode(objectConfig.getProxySuffix(), "UTF-8");
            keyName = Encoder.urlEncodePath(keyName, "UTF-8");
            return new StringBuilder(objectConfig.getProtocol().getValue())
                    .append(String.format("%s.%s.%s/%s", bucketName, region, proxySuffix, keyName)).toString();
        } catch (UnsupportedEncodingException e) {
            throw new UfileClientException("Occur error during URLEncode bucketName and keyName", e);
        }
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
