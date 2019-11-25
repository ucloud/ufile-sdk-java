package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.bean.CopyObjectResultBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.PutJsonRequestBuilder;
import cn.ucloud.ufile.util.HttpMethod;
import com.google.gson.JsonElement;
import okhttp3.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

/**
 * API-云端对象文件拷贝
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 201/11/12 19:08
 */
public class CopyObjectApi extends UfileObjectApi<CopyObjectResultBean> {

    /**
     * Required
     * 要复制的来源Bucket空间名称
     */
    protected String srcBucketName;

    /**
     * Required
     * 要复制的来源云端对象名称
     */
    protected String srcKeyName;

    /**
     * Required
     * 要复制到的目标Bucket空间名称
     */
    protected String dstBucketName;

    /**
     * Required
     * 要复制到的目标云端对象名称
     */
    protected String dstKeyName;

    /**
     * 构造方法
     *
     * @param authorizer Object授权器
     * @param host       API域名
     * @param httpClient Http客户端
     */
    protected CopyObjectApi(ObjectAuthorizer authorizer, String host, HttpClient httpClient) {
        super(authorizer, host, httpClient);
    }

    /**
     * 设置要复制的源对象信息
     *
     * @param srcBucketName 要复制的源对象bucket名称
     * @param srcKeyName    要复制的源对象文件名称
     * @return {@link CopyObjectApi}
     */
    public CopyObjectApi from(String srcBucketName, String srcKeyName) {
        this.srcBucketName = srcBucketName;
        this.srcKeyName = srcKeyName;
        return this;
    }

    /**
     * 设置要复制到的目标对象信息
     *
     * @param dstBucketName 要复制到的目标对象bucket名称
     * @param dstKeyName    要复制到的目标对象文件名称
     * @return {@link CopyObjectApi}
     */
    public CopyObjectApi copyTo(String dstBucketName, String dstKeyName) {
        this.dstBucketName = dstBucketName;
        this.dstKeyName = dstKeyName;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public CopyObjectApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileClientException {
        parameterValidat();

        contentType = "application/json; charset=utf-8";
        String contentMD5 = "";
        String date = dateFormat.format(new Date(System.currentTimeMillis()));
        String xUfileCopySource = null;
        try {
            xUfileCopySource = new StringBuilder("/")
                    .append(URLEncoder.encode(srcBucketName, "UTF-8").replace("+", "%20"))
                    .append("/")
                    .append(URLEncoder.encode(srcKeyName, "UTF-8").replace("+", "%20"))
                    .toString();
        } catch (UnsupportedEncodingException e) {
            throw new UfileClientException("Occur error during URLEncode srcBucketName and srcKeyName", e);
        }

        PutJsonRequestBuilder builder = (PutJsonRequestBuilder) new PutJsonRequestBuilder()
                .setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut)
                .baseUrl(generateFinalHost(dstBucketName, dstKeyName))
                .addHeader("Content-Type", contentType)
                .addHeader("Accpet", "*/*")
                .addHeader("X-Ufile-Copy-Source", xUfileCopySource)
                .addHeader("Date", date);

        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.PUT,
                dstBucketName, dstKeyName, contentType, contentMD5, date).setOptional(authOptionalData));

        builder.addHeader("authorization", authorization);

        call = builder.build(httpClient.getOkHttpClient());
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (srcBucketName == null || srcBucketName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'srcBucketName' can not be null or empty");

        if (srcKeyName == null || srcKeyName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'srcKeyName' can not be null or empty");

        if (dstBucketName == null || dstBucketName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'dstBucketName' can not be null or empty");

        if (dstKeyName == null || dstKeyName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'dstKeyName' can not be null or empty");
    }

    @Override
    public CopyObjectResultBean parseHttpResponse(Response response) throws UfileClientException, UfileServerException {
        CopyObjectResultBean res = super.parseHttpResponse(response);
        if (res != null && res.geteTag() != null) {
            res.seteTag(res.geteTag().replace("\"", ""));
        }
        return res;
    }
}
