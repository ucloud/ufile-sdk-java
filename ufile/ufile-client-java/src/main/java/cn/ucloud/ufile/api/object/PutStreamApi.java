package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.UfileConstants;
import cn.ucloud.ufile.api.object.policy.PutPolicy;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.bean.PutObjectResultBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.*;
import cn.ucloud.ufile.http.BaseHttpCallback;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.ProgressConfig;
import cn.ucloud.ufile.http.request.PutStreamRequestBuilder;
import cn.ucloud.ufile.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import okhttp3.MediaType;
import okhttp3.Response;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * API-Put上传流
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:08
 */
public class PutStreamApi extends UfileObjectApi<PutObjectResultBean> {
    /**
     * Required
     * 云端对象名称
     */
    protected String keyName;
    /**
     * Required
     * 要上传的流
     */
    protected InputStream inputStream;
    /**
     * Required
     * 要上传的流mimeType
     */
    protected String mimeType;
    /**
     * Required
     * 根据MimeType解析成okhttp可用的mediaType，解析失败则代表mimeType无效
     */
    protected MediaType mediaType;

    /**
     * Required
     * Bucket空间名称
     */
    protected String bucketName;
    /**
     * 是否需要上传MD5校验码
     */
    private boolean isVerifyMd5 = true;

    private ProgressConfig progressConfig;

    private ByteArrayOutputStream cacheOutputStream;

    /**
     * 流写入的buffer大小，Default = 256 KB
     */
    private int bufferSize = UfileConstants.DEFAULT_BUFFER_SIZE;

    /**
     * UFile上传回调策略
     */
    private PutPolicy putPolicy;

    /**
     * 用户自定义文件元数据
     */
    protected Map<String, String> metadatas;

    /**
     * 文件存储类型，分别是标准、低频、冷存，对应有效值：STANDARD | IA | ARCHIVE
     */
    protected String storageType;


    /**
     * 构造方法
     *
     * @param authorizer   Object授权器
     * @param objectConfig ObjectConfig {@link ObjectConfig}
     * @param httpClient   Http客户端
     */
    protected PutStreamApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
        progressConfig = ProgressConfig.callbackDefault();
    }

    /**
     * 设置上传到云端的对象名称
     *
     * @param keyName 对象名称
     * @return {@link PutStreamApi}
     */
    public PutStreamApi nameAs(String keyName) {
        this.keyName = keyName;
        return this;
    }

    /**
     * 设置要上传的流和MIME类型
     *
     * @param inputStream 输入流
     * @param mimeType    MIME类型
     * @return {@link PutStreamApi}
     */
    public PutStreamApi from(InputStream inputStream, String mimeType) {
        this.inputStream = inputStream;
        this.mimeType = mimeType;
        this.mediaType = MediaType.parse(mimeType);
        return this;
    }

    /**
     * 设置要上传到的Bucket名称
     *
     * @param bucketName bucket名称
     * @return
     */
    public PutStreamApi toBucket(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * 设置是否需要MD5校验
     *
     * @param isVerifyMd5 是否校验MD5
     * @return {@link PutStreamApi}
     */
    public PutStreamApi withVerifyMd5(boolean isVerifyMd5) {
        this.isVerifyMd5 = isVerifyMd5;
        return this;
    }

    /**
     * 配置进度回调设置
     *
     * @param config 进度回调设置
     * @return {@link PutStreamApi}
     */
    public PutStreamApi withProgressConfig(ProgressConfig config) {
        progressConfig = config == null ? this.progressConfig : config;
        return this;
    }

    /**
     * 设置流读写的Buffer大小，默认 256 KB
     *
     * @param bufferSize Buffer大小
     * @return {@link PutStreamApi}
     */
    public PutStreamApi setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return
     */
    public PutStreamApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }

    /**
     * 设置上传回调策略
     *
     * @param putPolicy 上传回调策略
     * @return {@link PutStreamApi}
     */
    public PutStreamApi withPutPolicy(PutPolicy putPolicy) {
        this.putPolicy = putPolicy;
        return this;
    }

    /**
     * 为云端对象配置自定义数据，每次调用将会替换之前数据。
     * 默认为null，若配置null则表示取消配置自定义数据
     * <p>
     * 所有的自定义数据总大小不能超过 8KB。
     *
     * @param datas 自定义数据，Key：不能为null和""，并且只支持字母大小写、数字和减号分隔符"-"  {@link List <Parameter>}
     */
    public PutStreamApi withMetaDatas(Map<String, String> datas) {
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
     * @param data 自定义数据，Key：不能为null和""，并且只支持字母大小写、数字和减号分隔符"-"  {@link Parameter<String>}
     */
    public PutStreamApi addMetaData(Parameter<String> data) {
        if (data == null)
            return this;

        if (metadatas == null)
            metadatas = new HashMap<>();

        metadatas.put(data.key, data.value);
        return this;
    }

    /**
     * 配置文件存储类型，分别是标准、低频、冷存，对应有效值：STANDARD | IA | ARCHIVE
     *
     * @param storageType 文件存储类型，{@link StorageType}
     */
    public PutStreamApi withStorageType(String storageType) {
        this.storageType = storageType;
        return this;
    }

    @Override
    protected void prepareData() throws UfileClientException {
        try {
            parameterValidat();

            String contentType = mediaType.toString();
            String contentMD5 = "";
            String date = dateFormat.format(new Date(System.currentTimeMillis()));

            PutStreamRequestBuilder builder = (PutStreamRequestBuilder) new PutStreamRequestBuilder(onProgressListener)
                    .setBufferSize(bufferSize)
                    .setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut)
                    .baseUrl(generateFinalHost(bucketName, keyName))
                    .addHeader("Content-Type", contentType)
                    .addHeader("Accpet", "*/*")
                    .addHeader("Date", date)
                    .mediaType(mediaType);

            builder.addHeader("Content-Length", String.valueOf(inputStream.available()));

            if (storageType != null)
                builder.addHeader("X-Ufile-Storage-Class", storageType);

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

            if (isVerifyMd5) {
                try {
                    backupStream();
                    contentMD5 = HexFormatter.formatByteArray2HexString(Encoder.md5(new ByteArrayInputStream(cacheOutputStream.toByteArray())), false);
                    builder.addHeader("Content-MD5", contentMD5);
                    inputStream = new ByteArrayInputStream(cacheOutputStream.toByteArray());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            String authorization = authorizer.authorization((ObjectOptAuthParam) new ObjectOptAuthParam(HttpMethod.PUT, bucketName, keyName,
                    contentType, contentMD5, date).setPutPolicy(putPolicy).setOptional(authOptionalData));

            builder.addHeader("authorization", authorization);

            builder.params(inputStream);
            builder.setProgressConfig(progressConfig);

            FileUtil.close(cacheOutputStream);

            call = builder.build(httpClient.getOkHttpClient());
        } catch (IOException e) {
            throw new UfileIOException(e.getMessage());
        } catch (UfileClientException e) {
            throw e;
        }
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (inputStream == null)
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'inputStream' can not be null");

        if (keyName == null || keyName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'keyName' can not be null or empty");

        if (mimeType == null || mimeType.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'mimeType' can not be null or empty");

        if (mediaType == null)
            throw new UfileParamException(
                    "The required param 'mimeType' is invalid");

        if (bucketName == null || bucketName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'bucketName' can not be null or empty");
    }

    private void backupStream() {
        cacheOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[128 << 10];
        int len = 0;
        try {
            while ((len = inputStream.read(buff)) > 0) {
                cacheOutputStream.write(buff, 0, len);
            }

            cacheOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(inputStream);
        }
    }

    private OnProgressListener onProgressListener;

    /**
     * 配置进度监听器
     * 该配置可供execute()同步接口回调进度使用，若使用executeAsync({@link BaseHttpCallback})，则后配置的会覆盖新配置的
     *
     * @param onProgressListener 进度监听器
     * @return {@link PutStreamApi}
     */
    public PutStreamApi setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
        return this;
    }

    @Override
    public void executeAsync(BaseHttpCallback<PutObjectResultBean, UfileErrorBean> callback) {
        onProgressListener = callback;
        super.executeAsync(callback);
    }

    @Override
    public PutObjectResultBean parseHttpResponse(Response response) {
        PutObjectResultBean result = new PutObjectResultBean();
        String eTag = response.header("ETag", null);
        eTag = eTag == null ? null : eTag.replace("\"", "");
        result.seteTag(eTag);

        if (putPolicy != null) {
            result.setCallbackRet(readResponseBody(response));
        }

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

    @Override
    public UfileErrorBean parseErrorResponse(Response response) throws UfileClientException {
        UfileErrorBean errorBean = null;
        if (putPolicy != null) {
            String content = readResponseBody(response);
            response.body().close();
            try {
                errorBean = new Gson().fromJson((content == null || content.length() == 0) ? "{}" : content, UfileErrorBean.class);
            } catch (Exception e) {
                errorBean = new UfileErrorBean();
            }
            errorBean.setResponseCode(response.code());
            errorBean.setxSessionId(response.header("X-SessionId"));
            errorBean.setCallbackRet(content);
            return errorBean;
        } else {
            errorBean = super.parseErrorResponse(response);
        }
        return errorBean;
    }
}
