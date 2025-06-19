package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.multi.*;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.auth.ObjectRemoteAuthorization;
import cn.ucloud.ufile.auth.UfileAuthorizationException;
import cn.ucloud.ufile.auth.sign.UfileSignatureException;
import cn.ucloud.ufile.bean.ObjectProfile;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileIOException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.util.Etag;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Object相关API构造器
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/11 15:14
 */
public class ObjectApiBuilder {
    protected UfileClient client;
    protected ObjectAuthorizer authorizer;
    protected ObjectConfig objectConfig;
    protected Map<String, String> headers;
    protected String securityToken;

    public ObjectApiBuilder(UfileClient client, ObjectAuthorizer authorizer, ObjectConfig objectConfig) {
        this.client = client;
        this.authorizer = authorizer;
        if (authorizer instanceof ObjectRemoteAuthorization)
            ((ObjectRemoteAuthorization) authorizer).setHttpClient(client.getHttpClient());
        this.objectConfig = objectConfig;
    }

    /**
     * 设置 HTTP 头
     *
     * @param headers HTTP 头
     * @return {@link ObjectApiBuilder}
     */
    public ObjectApiBuilder withHeaders(Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            this.headers = headers;
        }
        return this;
    }

    /**
     * 设置安全令牌（STS临时凭证）
     *
     * @param securityToken 安全令牌
     * @return {@link ObjectApiBuilder}
     */
    public ObjectApiBuilder withSecurityToken(String securityToken) {
        this.securityToken = securityToken;
        return this;
    }

    /**
     * 获取公有空间对象的下载URL
     *
     * @param keyName    目标对象名
     * @param bucketName 空间名
     * @return {@link GenerateObjectPublicUrlApi}
     * @throws UfileRequiredParamNotFoundException
     */
    public GenerateObjectPublicUrlApi getDownloadUrlFromPublicBucket(String keyName, String bucketName) {
        return new GenerateObjectPublicUrlApi(objectConfig, keyName, bucketName);
    }

    /**
     * 获取私有空间对象的下载URL
     *
     * @param keyName         目标对象名
     * @param bucketName      空间名
     * @param expiresDuration 有效时限 (当前时间开始计算的一个有效时间段, 单位：Unix time second。 eg: 24*60*60 = 1天有效)
     * @return {@link GenerateObjectPrivateUrlApi}
     * @throws UfileSignatureException
     * @throws UfileRequiredParamNotFoundException
     * @throws UfileAuthorizationException
     */
    public GenerateObjectPrivateUrlApi getDownloadUrlFromPrivateBucket(String keyName, String bucketName, int expiresDuration) {
        return new GenerateObjectPrivateUrlApi(authorizer, objectConfig, keyName, bucketName, expiresDuration);
    }

    /**
     * Get下载文件
     *
     * @param downloadUrl 下载地址
     *                    根据所保存的bucket从{@link this.getDownloadUrlFromPublicBucket}和{@link this.getDownloadUrlFromPrivateBucket}获取
     * @return {@link GetFileApi}
     */
    public GetFileApi getFile(String downloadUrl) {
        GetFileApi getFileApi = new GetFileApi(authorizer, new ObjectConfig(downloadUrl), client.getHttpClient());
        
        // 将 headers 传递给 GetFileApi
        if (headers != null && !headers.isEmpty()) {
            getFileApi.SetHttpHeaders(headers);
        }
        
        
        if (securityToken != null && !securityToken.isEmpty()) {
            getFileApi.withSecurityToken(securityToken);
        }
        
        return getFileApi;
    }

    /**
     * Get下载流
     *
     * @param downloadUrl 下载地址
     *                    根据所保存的bucket从{@link this.getDownloadUrlFromPublicBucket}和{@link this.getDownloadUrlFromPrivateBucket}获取
     * @return {@link GetStreamApi}
     */
    public GetStreamApi getStream(String downloadUrl) {
        GetStreamApi getStreamApi = new GetStreamApi(authorizer, new ObjectConfig(downloadUrl), client.getHttpClient());
        
        // 将 headers 传递给 GetStreamApi
        if (headers != null && !headers.isEmpty()) {
            getStreamApi.SetHttpHeaders(headers);
        }
        
        
        if (securityToken != null && !securityToken.isEmpty()) {
            getStreamApi.withSecurityToken(securityToken);
        }
        
        return getStreamApi;
    }

    /**
     * put 文件
     *
     * @param file     本地文件
     * @param mimeType mime类型
     * @return {@link PutFileApi}
     */
    public PutFileApi putObject(File file, String mimeType) {
        PutFileApi putFileApi = new PutFileApi(authorizer, objectConfig, client.getHttpClient())
                .from(file, mimeType);
        
        // 将 headers 传递给 PutFileApi
        if (headers != null && !headers.isEmpty()) {
            putFileApi.SetHttpHeaders(headers);
        }
        
        
        if (securityToken != null && !securityToken.isEmpty()) {
            putFileApi.withSecurityToken(securityToken);
        }
        
        return putFileApi;
    }

    /**
     * put 流
     *
     * @param inputStream   输入流
     * @param contentLength 输入流的数据长度，bytesLength
     * @param mimeType      mime类型
     * @return {@link PutStreamApi}
     */
    public PutStreamApi putObject(InputStream inputStream, long contentLength, String mimeType) {
        PutStreamApi putStreamApi = new PutStreamApi(authorizer, objectConfig, client.getHttpClient())
                .from(inputStream, contentLength, mimeType);
        
        // 将 headers 传递给 PutStreamApi
        if (headers != null && !headers.isEmpty()) {
            putStreamApi.SetHttpHeaders(headers);
        }
        
        
        if (securityToken != null && !securityToken.isEmpty()) {
            putStreamApi.withSecurityToken(securityToken);
        }
        
        return putStreamApi;
    }

    /**
     * append 数据
     *
     * @param appendData 要append的数据
     * @param mimeType   mime类型
     * @return
     */
    public AppendObjectApi appendObject(byte[] appendData, String mimeType) {
        AppendObjectApi appendObjectApi = new AppendObjectApi(authorizer, objectConfig, client.getHttpClient())
                .from(appendData, mimeType);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            appendObjectApi.withSecurityToken(securityToken);
        }
        
        return appendObjectApi;
    }

    /**
     * 删除云端文件
     *
     * @param keyName    云端文件名
     * @param bucketName 空间名
     * @return {@link DeleteObjectApi}
     */
    public DeleteObjectApi deleteObject(String keyName, String bucketName) {
        DeleteObjectApi deleteObjectApi = new DeleteObjectApi(authorizer, objectConfig, client.getHttpClient())
                .keyName(keyName)
                .atBucket(bucketName);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            deleteObjectApi.withSecurityToken(securityToken);
        }
        
        return deleteObjectApi;
    }

    /**
     * 获取云端文件描述
     *
     * @param keyName    云端文件名
     * @param bucketName 空间名
     * @return {@link ObjectProfileApi}
     */
    public ObjectProfileApi objectProfile(String keyName, String bucketName) {
        ObjectProfileApi objectProfileApi = new ObjectProfileApi(authorizer, objectConfig, client.getHttpClient())
                .which(keyName)
                .atBucket(bucketName);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            objectProfileApi.withSecurityToken(securityToken);
        }
        
        return objectProfileApi;
    }


    /**
     * 获取分片上传parts信息
     * @param uploadId 空间名
     * @param bucketName 空间名
     * @return {@link ObjectListApi}
     */
    public MultiUploadListPartsInfoApi multiUploadListPartsInfo(String uploadId, String bucketName) {
        MultiUploadListPartsInfoApi api = new MultiUploadListPartsInfoApi(authorizer, objectConfig, client.getHttpClient()).setUploadId(uploadId)
                .atBucket(bucketName);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            api.withSecurityToken(securityToken);
        }
        
        return api;
    }

    /**
     * 获取文件列表
     *
     * @param bucketName 空间名
     * @return {@link ObjectListApi}
     */
    public ObjectListApi objectList(String bucketName) {
        ObjectListApi api = new ObjectListApi(authorizer, objectConfig, client.getHttpClient())
                .atBucket(bucketName);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            api.withSecurityToken(securityToken);
        }
        
        return api;
    }

    /**
     * 获取目录格式的文件列表
     *
     * @param bucketName 空间名
     * @return {@link ObjectListWithDirFormatApi}
     */
    public ObjectListWithDirFormatApi objectListWithDirFormat(String bucketName) {
        ObjectListWithDirFormatApi api = new ObjectListWithDirFormatApi(authorizer, objectConfig, client.getHttpClient())
                .atBucket(bucketName);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            api.withSecurityToken(securityToken);
        }
        
        return api;
    }

    /**
     * 初始化分片上传
     *
     * @param keyName    目标对象名
     * @param mimeType   mime类型
     * @param bucketName 空间名
     * @return {@link InitMultiUploadApi}
     */
    public InitMultiUploadApi initMultiUpload(String keyName, String mimeType, String bucketName) {
        InitMultiUploadApi api = new InitMultiUploadApi(authorizer, objectConfig, client.getHttpClient())
                .nameAs(keyName)
                .withMimeType(mimeType)
                .toBucket(bucketName);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            api.withSecurityToken(securityToken);
        }
        
        return api;
    }

    /**
     * 上传分片
     *
     * @param state     分片上传状态
     * @param part      分片数据
     * @param partIndex 分片序号
     * @return {@link MultiUploadPartApi}
     */
    public MultiUploadPartApi multiUploadPart(MultiUploadInfo state, byte[] part, int partIndex) {
        MultiUploadPartApi api = new MultiUploadPartApi(authorizer, objectConfig, client.getHttpClient())
                .which(state)
                .from(part, partIndex);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            api.withSecurityToken(securityToken);
        }
        
        return api;
    }

    /**
     * 上传分片
     *
     * @param state     分片上传状态
     * @param part      分片数据
     * @param offset    分片数据偏移量
     * @param length    分片数据长度
     * @param partIndex 分片序号
     * @return {@link MultiUploadPartApi}
     */
    public MultiUploadPartApi multiUploadPart(MultiUploadInfo state, byte[] part, int offset, int length, int partIndex) {
        MultiUploadPartApi api = new MultiUploadPartApi(authorizer, objectConfig, client.getHttpClient())
                .which(state)
                .from(part, offset, length, partIndex);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            api.withSecurityToken(securityToken);
        }
        
        return api;
    }

    /**
     * 拷贝分片
     *
     * @param state           分片上传状态
     * @param partIndex       分片序号
     * @param sourceBucketName 源bucket名称
     * @param sourceObjectName 源object名称
     * @param rangeStart      源object起始位置
     * @param rangeEnd        源object结束位置
     * @return {@link UploadCopyPartApi}
     */
    public UploadCopyPartApi multiUploadCopyPart(MultiUploadInfo state, int partIndex, String sourceBucketName, String sourceObjectName, long rangeStart, long rangeEnd) {
        UploadCopyPartApi api = new UploadCopyPartApi(authorizer, objectConfig, client.getHttpClient())
                .which(state)
                .from(partIndex, sourceBucketName, sourceObjectName, rangeStart, rangeEnd);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            api.withSecurityToken(securityToken);
        }
        
        return api;
    }

    /**
     * 终止分片上传
     *
     * @param state 分片上传状态
     * @return {@link AbortMultiUploadApi}
     */
    public AbortMultiUploadApi abortMultiUpload(MultiUploadInfo state) {
        AbortMultiUploadApi api = new AbortMultiUploadApi(authorizer, objectConfig, client.getHttpClient())
                .which(state);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            api.withSecurityToken(securityToken);
        }
        
        return api;
    }

    /**
     * 完成分片上传
     *
     * @param state      分片上传状态
     * @param partStates 分片上传结果集合
     * @return {@link FinishMultiUploadApi}
     */
    public FinishMultiUploadApi finishMultiUpload(MultiUploadInfo state, List<MultiUploadPartState> partStates) {
        FinishMultiUploadApi api = new FinishMultiUploadApi(authorizer, objectConfig, client.getHttpClient())
                .which(state, partStates);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            api.withSecurityToken(securityToken);
        }
        
        return api;
    }

    /**
     * 文件下载
     *
     * @param profile 云端对象信息
     * @return {@link DownloadFileApi}
     */
    public DownloadFileApi downloadFile(ObjectProfile profile) {
        DownloadFileApi api = new DownloadFileApi(authorizer, objectConfig, client.getHttpClient())
                .which(profile);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            api.withSecurityToken(securityToken);
        }
        
        return api;
    }

    /**
     * 解冻归档类型的云端对象
     *
     * @param keyName    目标对象名
     * @param bucketName 空间名
     * @return {@link ObjectRestoreApi}
     */
    public ObjectRestoreApi objectRestore(String keyName, String bucketName) {
        ObjectRestoreApi api = new ObjectRestoreApi(authorizer, objectConfig, client.getHttpClient())
                .which(keyName)
                .atBucket(bucketName);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            api.withSecurityToken(securityToken);
        }
        
        return api;
    }

    /**
     * 拷贝文件
     *
     * @param srcBucket  源bucket名称
     * @param srcKeyName 源key名称
     * @return {@link CopyObjectApi}
     */
    public CopyObjectApi copyObject(String srcBucket, String srcKeyName) {
        CopyObjectApi api = new CopyObjectApi(authorizer, objectConfig, client.getHttpClient())
                .from(srcBucket, srcKeyName);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            api.withSecurityToken(securityToken);
        }
        
        return api;
    }

    /**
     * 修改对象存储类型
     *
     * @param bucketName 空间名
     * @param keyName    目标对象名
     * @return {@link StorageTypeSwitchApi}
     */
    public StorageTypeSwitchApi switchStorageType(String bucketName, String keyName) {
        StorageTypeSwitchApi api = new StorageTypeSwitchApi(authorizer, objectConfig, client.getHttpClient())
                .which(bucketName, keyName);
                
        
        if (securityToken != null && !securityToken.isEmpty()) {
            api.withSecurityToken(securityToken);
        }
        
        return api;
    }

    /**
     * 比对ETag值
     *
     * @param localFile  要对比的本地文件
     * @param keyName    要对比的云端文件名
     * @param bucketName 要对比的云端文件的所属空间
     * @return ETag是否一致
     * @throws UfileClientException
     */
    public boolean compareEtag(File localFile, String keyName, String bucketName)
            throws UfileClientException, UfileServerException {
        try {
            return compareEtag(new FileInputStream(localFile), keyName, bucketName);
        } catch (FileNotFoundException e) {
            throw new UfileIOException(e);
        }
    }

    /**
     * 比对ETag值
     *
     * @param localStream 要对比的本地流
     * @param keyName     要对比的云端文件名
     * @param bucketName  要对比的云端文件的所属空间
     * @return ETag是否一致
     * @throws UfileClientException
     */
    public boolean compareEtag(InputStream localStream, String keyName, String bucketName)
            throws UfileClientException, UfileServerException {
        ObjectProfile res = objectProfile(keyName, bucketName).execute();
        try {
            Etag eTag = Etag.etag(localStream);
            return eTag.geteTag().equals(res.geteTag());
        } catch (IOException e) {
            throw new UfileIOException(e);
        }
    }
}
