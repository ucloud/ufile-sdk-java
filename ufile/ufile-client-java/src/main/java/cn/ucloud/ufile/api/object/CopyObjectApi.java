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
import cn.ucloud.ufile.util.MetadataDirective;
import cn.ucloud.ufile.util.Parameter;
import com.google.gson.JsonElement;
import okhttp3.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

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
     * 用户自定义文件元数据
     */
    protected Map<String, String> metadatas;

    /**
     * 用户自定义元数据设置方式
     */
    protected String metadataDirective;

    /**
     * 构造方法
     *
     * @param authorizer   Object授权器
     * @param objectConfig ObjectConfig {@link ObjectConfig}
     * @param httpClient   Http客户端
     */
    protected CopyObjectApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
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

    /**
     * 为云端对象配置自定义数据，每次调用将会替换之前数据。
     * 默认为null，若配置null则表示取消配置自定义数据
     * <p>
     * 所有的自定义数据总大小不能超过 8KB。
     *
     * @param datas 自定义数据，Key：不能为null和""，并且只支持字母大小写、数字和减号分隔符"-"  {@link List<Parameter>}
     */
    public CopyObjectApi withMetaDatas(Map<String, String> datas) {
        if (datas == null) {
            metadatas = null;
            return this;
        }

        metadatas = new HashMap<>(datas);
        return this;
    }

    /**
     * 为云端对象添加自定义数据，可直接调用，无须先调用withMetaDatas
     * key不能为空或者""
     * <p>
     * 所有的自定义数据总大小不能超过 8KB。
     *
     * @param data 自定义数据，Key：不能为null和""，并且只支持字母大小写、数字和减号分隔符"-" {@link Parameter<String>}
     */
    public CopyObjectApi addMetaData(Parameter<String> data) {
        if (data == null)
            return this;

        if (metadatas == null)
            metadatas = new HashMap<>();

        metadatas.put(data.key, data.value);
        return this;
    }

    /**
     * 配置用户自定义元数据设置方式
     *
     * @param metadataDirective 用户自定义元数据设置方式 {@link MetadataDirective}
     */
    public CopyObjectApi withMetadataDirective(String metadataDirective) {
        this.metadataDirective = metadataDirective;
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

        if (metadataDirective != null)
            builder.addHeader("X-Ufile-Metadata-Directive", metadataDirective);

        if (metadatas != null && !metadatas.isEmpty()) {
            Set<String> keys = metadatas.keySet();
            if (keys != null) {
                for (String key : keys) {
                    if (key == null || key.isEmpty())
                        continue;

                    String value = metadatas.get(key);
                    builder.addHeader(new StringBuilder("X-Ufile-Meta-").append(key).toString(), value == null ? "" : value);
                }
            }
        }

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
        CopyObjectResultBean result = super.parseHttpResponse(response);
        String eTag = response.header("ETag", null);
        eTag = eTag == null ? null : eTag.replace("\"", "");
        result.seteTag(eTag);

        if (response.headers() != null) {
            Set<String> names = response.headers().names();
            if (names != null) {
                Map<String, String> headers = new HashMap<>();
                for (String name : names) {
                    headers.put(name, response.header(name, null));
                }
                result.setHeaders(headers);
            }
        }

        return result;
    }
}
