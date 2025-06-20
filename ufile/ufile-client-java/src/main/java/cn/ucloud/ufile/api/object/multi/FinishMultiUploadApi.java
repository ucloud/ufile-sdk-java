package cn.ucloud.ufile.api.object.multi;

import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.UfileObjectApi;
import cn.ucloud.ufile.api.object.policy.PutPolicy;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.bean.MultiUploadResponse;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.*;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.request.PostStringRequestBuilder;
import cn.ucloud.ufile.util.FileUtil;
import cn.ucloud.ufile.util.HttpMethod;
import cn.ucloud.ufile.util.MetadataDirective;
import cn.ucloud.ufile.util.Parameter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import okhttp3.MediaType;
import okhttp3.Response;

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
     * UFile上传回调策略
     */
    private PutPolicy putPolicy;

    /**
     * 用户自定义文件元数据
     */
    protected Map<String, String> metadatas;

    /**
     * 用户自定义元数据设置方式
     */
    protected String metadataDirective;

    /**
     * 安全令牌（STS临时凭证）
     */
    private String securityToken;

    /**
     * 构造方法
     *
     * @param authorizer   Object授权器
     * @param objectConfig ObjectConfig {@link ObjectConfig}
     * @param httpClient   Http客户端
     */
    public FinishMultiUploadApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
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
     * 设置安全令牌（STS临时凭证）
     *
     * @param securityToken 安全令牌
     * @return {@link FinishMultiUploadApi}
     */
    public FinishMultiUploadApi withSecurityToken(String securityToken) {
        this.securityToken = securityToken;
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

    /**
     * 设置上传回调策略
     *
     * @param putPolicy 上传回调策略
     * @return {@link FinishMultiUploadApi}
     */
    public FinishMultiUploadApi withPutPolicy(PutPolicy putPolicy) {
        this.putPolicy = putPolicy;
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
    public FinishMultiUploadApi withMetaDatas(Map<String, String> datas) {
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
    public FinishMultiUploadApi addMetaData(Parameter<String> data) {
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
    public FinishMultiUploadApi withMetadataDirective(String metadataDirective) {
        this.metadataDirective = metadataDirective;
        return this;
    }

    @Override
    protected void prepareData() throws UfileClientException {
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
            bodyBuffer.append(part.geteTag() + (i < (len - 1) ? "," : ""));
        }

        contentType = MediaType.parse(info.getMimeType()).toString();
        String date = dateFormat.format(new Date(System.currentTimeMillis()));
        String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.POST, info.getBucket(), info.getKeyName(),
                contentType, "", date).setPutPolicy(putPolicy).setOptional(authOptionalData));

        builder.baseUrl(builder.generateGetUrl(generateFinalHost(info.getBucket(), info.getKeyName()), query))
                .addHeader("Content-Type", contentType)
                .addHeader("Content-Length", String.valueOf(bodyBuffer.length()))
                .addHeader("Accpet", "*/*")
                .addHeader("Date", date)
                .addHeader("authorization", authorization);

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
        

        if (securityToken != null && !securityToken.isEmpty()) {
            builder.addHeader("SecurityToken", securityToken);
        }

        builder.params(bodyBuffer.toString());
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

    @Override
    public MultiUploadResponse parseHttpResponse(Response response) throws UfileServerException, UfileClientException {
        MultiUploadResponse result = null;

        try {
            if (putPolicy != null) {
                result = new MultiUploadResponse();
                result.setCallbackRet(readResponseBody(response));
            } else {
                result = super.parseHttpResponse(response);
                String eTag = response.header("ETag");
                eTag = eTag == null ? null : eTag.replace("\"", "");
                result.seteTag(eTag);
            }
        } finally {
            FileUtil.close(response.body());
        }

        return result;
    }

    @Override
    public UfileErrorBean parseErrorResponse(Response response) throws UfileClientException {
        UfileErrorBean errorBean = null;
        try {
            if (putPolicy != null) {
                String content = readResponseBody(response);
                try {
                    errorBean = new Gson().fromJson((content == null || content.length() == 0) ? "{}" : content, UfileErrorBean.class);
                } catch (Exception e) {
                    errorBean = new UfileErrorBean();
                    errorBean.setErrMsg(content);
                }
                errorBean.setResponseCode(response.code());
                errorBean.setxSessionId(response.header("X-SessionId"));
                errorBean.setCallbackRet(content);
                return errorBean;
            } else {
                errorBean = super.parseErrorResponse(response);
            }
        } finally {
            FileUtil.close(response.body());
        }
        return errorBean;
    }
}
