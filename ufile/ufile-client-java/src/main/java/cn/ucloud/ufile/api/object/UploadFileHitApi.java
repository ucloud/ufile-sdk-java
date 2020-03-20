package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.UfileConstants;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.bean.base.BaseBucketResponseBean;
import cn.ucloud.ufile.bean.base.BaseObjectResponseBean;
import cn.ucloud.ufile.exception.*;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.PostJsonRequestBuilder;
import cn.ucloud.ufile.util.*;
import com.google.gson.JsonElement;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * API-文件秒传
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:08
 */
public class UploadFileHitApi extends UfileObjectApi<BaseObjectResponseBean> {
    /**
     * Required
     * 云端对象名称
     */
    protected String keyName;
    /**
     * Required
     * 要上传的文件
     */
    private File file;
    /**
     * Required
     * Bucket空间名称
     */
    protected String bucketName;

    /**
     * 构造方法
     *
     * @param authorizer   Object授权器
     * @param objectConfig ObjectConfig {@link ObjectConfig}
     * @param httpClient   Http客户端
     */
    protected UploadFileHitApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
    }

    /**
     * 设置上传到云端的对象名称
     *
     * @param keyName 对象名称
     * @return {@link UploadFileHitApi}
     */
    public UploadFileHitApi nameAs(String keyName) {
        this.keyName = keyName;
        return this;
    }

    /**
     * 设置要上传的文件
     *
     * @param file 需上传的文件
     * @return {@link UploadFileHitApi}
     */
    public UploadFileHitApi from(File file) {
        this.file = file;
        return this;
    }

    /**
     * 设置要上传到的Bucket名称
     *
     * @param bucketName bucket名称
     * @return {@link UploadFileHitApi}
     */
    public UploadFileHitApi toBucket(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public UploadFileHitApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    @Override
    protected void prepareData() throws UfileClientException {
        parameterValidat();

        String date = dateFormat.format(new Date(System.currentTimeMillis()));
        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.POST, bucketName, keyName,
                contentType, "", date).setOptional(authOptionalData));

        PostJsonRequestBuilder builder = new PostJsonRequestBuilder();

        String url = generateFinalHost(bucketName, "uploadhit");
        List<Parameter<String>> query = new ArrayList<>();
        try {
            Etag etag = Etag.etag(file, UfileConstants.MULTIPART_SIZE);
            query.add(new Parameter<>("Hash", etag == null ? null : etag.geteTag()));
        } catch (IOException e) {
            throw new UfileIOException("Calculate ETag failed!", e);
        }
        query.add(new Parameter<>("FileName", keyName));
        query.add(new Parameter<>("FileSize", String.valueOf(file.length())));

        call = builder.baseUrl(builder.generateGetUrl(url, query))
                .setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut)
                .addHeader("Content-Type", contentType)
                .addHeader("Accpet", "*/*")
                .addHeader("Date", date)
                .addHeader("authorization", authorization)
                .build(httpClient.getOkHttpClient());
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (file == null)
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'file' can not be null");

        if (keyName == null || keyName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'keyName' can not be null or empty");

        if (bucketName == null || bucketName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'bucketName' can not be null or empty");
    }

    @Override
    public BaseObjectResponseBean parseHttpResponse(Response response) throws UfileClientException, UfileServerException {
        BaseObjectResponseBean result = super.parseHttpResponse(response);

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
