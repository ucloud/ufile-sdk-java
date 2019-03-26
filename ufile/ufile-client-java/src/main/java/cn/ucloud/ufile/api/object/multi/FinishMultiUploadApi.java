package cn.ucloud.ufile.api.object.multi;

import cn.ucloud.ufile.api.object.UfileObjectApi;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.auth.UfileAuthorizationException;
import cn.ucloud.ufile.auth.sign.UfileSignatureException;
import cn.ucloud.ufile.bean.MultiUploadResponse;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.PostStringRequestBuilder;
import cn.ucloud.ufile.util.HttpMethod;
import cn.ucloud.ufile.util.JLog;
import cn.ucloud.ufile.util.Parameter;
import com.google.gson.JsonElement;
import okhttp3.MediaType;

import java.util.*;

/**
 * API-完成分片上传
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:08
 */
public class FinishMultiUploadApi extends UfileObjectApi<MultiUploadResponse> {
    /**
     * Required
     * 分片上传初始化状态
     */
    private MultiUploadInfo info;
    /**
     * Required
     * 上传分片的结果集合
     */
    private List<MultiUploadPartState> partStates;

    /**
     * Optional
     * 新名称，由于分片上传流程是独立的，为了避免当分片上传完成时，初始化的keyName被后续文件占用，此时可选填newKeyName
     */
    protected String newKeyName;

    /**
     * 构造方法
     *
     * @param authorizer Object授权器
     * @param host       API域名
     * @param httpClient Http客户端
     */
    public FinishMultiUploadApi(ObjectAuthorizer authorizer, String host, HttpClient httpClient) {
        super(authorizer, host, httpClient);
    }

    /**
     * 配置需要完成的分片上传任务
     *
     * @param info       分片上传初始化信息，{@link MultiUploadInfo}
     * @param partStates 上传分片状态集合，{@link List<MultiUploadPartState>}
     * @return {@link FinishMultiUploadApi}
     */
    public FinishMultiUploadApi which(MultiUploadInfo info, List<MultiUploadPartState> partStates) {
        this.info = info;
        this.partStates = partStates;
        return this;
    }

    /**
     * 配置上传对象的新名称，由于分片上传流程是独立且对象较大，为了避免当分片上传完成时，初始化的keyName被后续文件占用，此时可选填newKeyName
     *
     * @param newKeyName 新名称
     * @return {@link FinishMultiUploadApi}
     */
    public FinishMultiUploadApi renameAs(String newKeyName) {
        this.newKeyName = newKeyName;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public FinishMultiUploadApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    /**
     * 上传分片结果集排序器
     */
    private Comparator<MultiUploadPartState> partStateComparator = new Comparator<MultiUploadPartState>() {
        @Override
        public int compare(MultiUploadPartState o1, MultiUploadPartState o2) {
            return o1.getPartIndex() > o2.getPartIndex() ? 1 : (o1.getPartIndex() == o2.getPartIndex() ? 0 : 1);
        }
    };

    @Override
    protected void prepareData() throws UfileParamException, UfileAuthorizationException, UfileSignatureException {
        parameterValidat();

        PostStringRequestBuilder builder = new PostStringRequestBuilder();
        List<Parameter<String>> query = new ArrayList<>();
        query.add(new Parameter<>("uploadId", info.getUploadId()));
        query.add(new Parameter<>("newKey", (newKeyName == null ? "" : newKeyName)));

        if (partStates == null)
            partStates = new ArrayList<>();

        Collections.sort(partStates, partStateComparator);

        StringBuffer bodyBuffer = new StringBuffer();
        for (int i = 0, len = partStates.size(); i < len; i++) {
            MultiUploadPartState part = partStates.get(i);
            JLog.T(TAG, part.toString());
            bodyBuffer.append(part.geteTag() + (i < (len - 1) ? "," : ""));
        }

        String contentType = MediaType.parse(info.getMimeType()).toString();
        String date = dateFormat.format(new Date(System.currentTimeMillis()));
        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.POST, info.getBucket(), info.getKeyName(),
                contentType, "", date).setOptional(authOptionalData));

        builder.baseUrl(builder.generateGetUrl(generateFinalHost(info.getBucket(), info.getKeyName()), query))
                .addHeader("Content-Type", contentType)
                .addHeader("Content-Length", String.valueOf(bodyBuffer.length()))
                .addHeader("Accpet", "*/*")
                .addHeader("Date", date)
                .addHeader("authorization", authorization)
                .params(bodyBuffer.toString());

        call = builder.build(httpClient.getOkHttpClient());
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (info == null)
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'info' can not be null");

        if (partStates == null)
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'partStates' can not be null");
    }
}
