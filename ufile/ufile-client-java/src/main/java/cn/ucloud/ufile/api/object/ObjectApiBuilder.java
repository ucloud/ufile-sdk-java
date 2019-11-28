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
    protected String host;

    public ObjectApiBuilder(UfileClient client, ObjectAuthorizer authorizer, String host) {
        this.client = client;
        this.authorizer = authorizer;
        if (authorizer instanceof ObjectRemoteAuthorization)
            ((ObjectRemoteAuthorization) authorizer).setHttpClient(client.getHttpClient());
        this.host = host;
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
        return new GenerateObjectPublicUrlApi(host, keyName, bucketName);
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
        return new GenerateObjectPrivateUrlApi(authorizer, host, keyName, bucketName, expiresDuration);
    }

    /**
     * Get下载文件
     *
     * @param downloadUrl 下载地址
     *                    根据所保存的bucket从{@link this.getDownloadUrlFromPublicBucket}和{@link this.getDownloadUrlFromPrivateBucket}获取
     * @return {@link GetFileApi}
     */
    public GetFileApi getFile(String downloadUrl) {
        return new GetFileApi(authorizer, downloadUrl, client.getHttpClient());
    }

    /**
     * Get下载流
     *
     * @param downloadUrl 下载地址
     *                    根据所保存的bucket从{@link this.getDownloadUrlFromPublicBucket}和{@link this.getDownloadUrlFromPrivateBucket}获取
     * @return {@link GetStreamApi}
     */
    public GetStreamApi getStream(String downloadUrl) {
        return new GetStreamApi(authorizer, downloadUrl, client.getHttpClient());
    }

    /**
     * put 文件
     *
     * @param file     本地文件
     * @param mimeType mime类型
     * @return {@link PutFileApi}
     */
    public PutFileApi putObject(File file, String mimeType) {
        return new PutFileApi(authorizer, host, client.getHttpClient())
                .from(file, mimeType);
    }

    /**
     * put 流
     *
     * @param inputStream 输入流
     * @param mimeType    mime类型
     * @return {@link PutStreamApi}
     */
    public PutStreamApi putObject(InputStream inputStream, String mimeType) {
        return new PutStreamApi(authorizer, host, client.getHttpClient())
                .from(inputStream, mimeType);
    }

    /**
     * append 数据
     *
     * @param appendData 要append的数据
     * @param mimeType   mime类型
     * @return
     */
    public AppendObjectApi appendObject(byte[] appendData, String mimeType) {
        return new AppendObjectApi(authorizer, host, client.getHttpClient())
                .from(appendData, mimeType);
    }

    /**
     * 文件秒传
     *
     * @param file 本地文件
     * @return {@link UploadFileHitApi}
     */
    public UploadFileHitApi uploadHit(File file) {
        return new UploadFileHitApi(authorizer, host, client.getHttpClient())
                .from(file);
    }

    /**
     * 流秒传
     *
     * @param inputStream 输入流
     * @return {@link UploadStreamHitApi}
     */
    public UploadStreamHitApi uploadHit(InputStream inputStream) {
        return new UploadStreamHitApi(authorizer, host, client.getHttpClient())
                .from(inputStream);
    }

    /**
     * 删除云端文件
     *
     * @param keyName    云端文件名
     * @param bucketName 空间名
     * @return {@link DeleteObjectApi}
     */
    public DeleteObjectApi deleteObject(String keyName, String bucketName) {
        return new DeleteObjectApi(authorizer, host, client.getHttpClient())
                .keyName(keyName)
                .atBucket(bucketName);
    }

    /**
     * 获取云端文件描述
     *
     * @param keyName    云端文件名
     * @param bucketName 空间名
     * @return {@link ObjectProfileApi}
     */
    public ObjectProfileApi objectProfile(String keyName, String bucketName) {
        return new ObjectProfileApi(authorizer, host, client.getHttpClient())
                .which(keyName)
                .atBucket(bucketName);
    }

    /**
     * 获取文件列表
     *
     * @param bucketName 空间名
     * @return {@link ObjectListApi}
     */
    public ObjectListApi objectList(String bucketName) {
        return new ObjectListApi(authorizer, host, client.getHttpClient())
                .atBucket(bucketName);
    }

    /**
     * 获取目录格式的文件列表
     *
     * @param bucketName 空间名
     * @return {@link ObjectListWithDirFormatApi}
     */
    public ObjectListWithDirFormatApi objectListWithDirFormat(String bucketName) {
        return new ObjectListWithDirFormatApi(authorizer, host, client.getHttpClient())
                .atBucket(bucketName);
    }

    /**
     * 分片上传-初始化
     *
     * @param keyName    目标对象名
     * @param mimeType   mime类型
     * @param bucketName 空间名
     * @return {@link InitMultiUploadApi}
     * @apiNote 分片上传必须首先执行本API，后续上传API均需要本API的返回{@link MultiUploadInfo}
     */
    public InitMultiUploadApi initMultiUpload(String keyName, String mimeType, String bucketName) {
        return new InitMultiUploadApi(authorizer, host, client.getHttpClient())
                .nameAs(keyName)
                .withMimeType(mimeType)
                .toBucket(bucketName);
    }

    /**
     * 分片上传-上传分片
     *
     * @param state     initMultiUpload 返回的response
     * @param part      分片数据
     * @param partIndex 分片序号
     * @return {@link MultiUploadPartApi}
     */
    public MultiUploadPartApi multiUploadPart(MultiUploadInfo state, byte[] part, int partIndex) {
        return new MultiUploadPartApi(authorizer, host, client.getHttpClient())
                .which(state)
                .from(part, partIndex);
    }

    /**
     * 分片上传-上传分片
     *
     * @param state     initMultiUpload 返回的response
     * @param part      分片数据
     * @param offset    分片数据偏移量
     * @param length    分片数据长度
     * @param partIndex 分片序号
     * @return {@link MultiUploadPartApi}
     */
    public MultiUploadPartApi multiUploadPart(MultiUploadInfo state, byte[] part, int offset, int length, int partIndex) {
        return new MultiUploadPartApi(authorizer, host, client.getHttpClient())
                .which(state)
                .from(part, offset, length, partIndex);
    }

    /**
     * 分片上传-中断上传
     *
     * @param state initMultiUpload 返回的response
     * @return {@link AbortMultiUploadApi}
     * @apiNote 分片上传初始化成功后，若上传分片时，任一分片上传失败，须调用本API进行中断处理
     */
    public AbortMultiUploadApi abortMultiUpload(MultiUploadInfo state) {
        return new AbortMultiUploadApi(authorizer, host, client.getHttpClient())
                .which(state);
    }

    /**
     * 分片上传-上传完成
     *
     * @param state      initMultiUpload 返回的response
     * @param partStates 所有分片调用multiUploadPart 返回的response 集合，{@link MultiUploadPartState}
     * @return {@link FinishMultiUploadApi}
     * @apiNote 分片上传初始化成功后，若上传分片时，任一分片上传失败，须调用本API进行中断处理
     */
    public FinishMultiUploadApi finishMultiUpload(MultiUploadInfo state, List<MultiUploadPartState> partStates) {
        return new FinishMultiUploadApi(authorizer, host, client.getHttpClient())
                .which(state, partStates);
    }

    /**
     * 下载文件
     *
     * @param profile 执行objectProfile 返回的response，包含了Ufile云上指定的文件信息，{@link ObjectProfile}
     * @return {@link DownloadFileApi}
     */
    public DownloadFileApi downloadFile(ObjectProfile profile) {
        return new DownloadFileApi(authorizer, host, client.getHttpClient()).which(profile);
    }

    /**
     * 归档存储的文件
     * 先进行 Restore 的解冻，才能进行下载
     *
     * @param keyName    云端文件名
     * @param bucketName 空间名
     * @return {@link ObjectRestoreApi}
     */
    public ObjectRestoreApi objectRestore(String keyName, String bucketName) {
        return new ObjectRestoreApi(authorizer, host, client.getHttpClient())
                .which(keyName)
                .atBucket(bucketName);
    }


    /**
     * 复制云端对象文件
     *
     * @param srcBucket  源bucket
     * @param srcKeyName 源文件名
     * @return {@link CopyObjectApi}
     */
    public CopyObjectApi copyObject(String srcBucket, String srcKeyName) {
        return new CopyObjectApi(authorizer, host, client.getHttpClient())
                .from(srcBucket, srcKeyName);
    }

    /**
     * 重命名云端对象文件
     *
     * @param bucketName 需要重命名的对象所在bucket
     * @param keyName    需要重命名的对象名称
     * @return {@link RenameObjectApi}
     */
    public RenameObjectApi renameObject(String bucketName, String keyName) {
        return new RenameObjectApi(authorizer, host, client.getHttpClient())
                .which(bucketName, keyName);
    }

    /**
     * 换存云端对象文件的转储类型
     *
     * @param bucketName 需要转换的对象所在bucket
     * @param keyName    需要转换的对象所在bucket
     * @return {@link StorageTypeSwitchApi}
     */
    public StorageTypeSwitchApi switchStorageType(String bucketName, String keyName) {
        return new StorageTypeSwitchApi(authorizer, host, client.getHttpClient())
                .which(bucketName, keyName);
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
