package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.policy.PolicyParam;
import cn.ucloud.ufile.api.object.policy.PutPolicy;
import cn.ucloud.ufile.api.object.policy.PutPolicyForCallback;
import cn.ucloud.ufile.bean.PutObjectResultBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.UfileCallback;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;
import cn.ucloud.ufile.util.MimeTypeUtil;
import cn.ucloud.ufile.util.StorageType;
import okhttp3.Request;

import java.io.*;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class PutObjectSample {
    private static final String TAG = "PutObjectSample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        InputStream is = new ByteArrayInputStream(new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07});
        // 如果上传File，则文件的MimeType可以使用MimeTypeUtil.getMimeType(File)来获取，MimeTypeUtil可能支持的type类型不全，用户可以按需自行填写
        File file = new File("");
        String keyName = "";
        String mimeType = MimeTypeUtil.getMimeType(keyName);
        String bucketName = "";
        putStream(is, mimeType, keyName, bucketName);
    }

    public static void putFile(File file, String mimeType, String nameAs, String toBucket) {
        try {
            /**
             * 上传回调策略
             * 必须填写回调接口url(目前仅支持http，不支持https)，可选填回调参数，回调参数请自行决定是否需要urlencode
             * 若配置上传回调，则上传接口的回调将会透传回调接口的response，包括httpCode
             */
            PutPolicy putPolicy = new PutPolicyForCallback.Builder("http://xxx.xxx.xxx.xxx[:port][/path]")
                    .addCallbackBody(new PolicyParam("key", "value"))
                    .build();
            PutObjectResultBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .putObject(file, mimeType)
                    .nameAs(nameAs)
                    .toBucket(toBucket)
                    /**
                     * 配置文件存储类型，分别是标准、低频、冷存，对应有效值：STANDARD | IA | ARCHIVE
                     */
                    .withStorageType(StorageType.STANDARD)
                    /**
                     * 为云端对象配置自定义数据，每次调用将会替换之前数据。
                     * 所有的自定义数据总大小不能超过 8KB。
                     */
//                    .withMetaDatas()
                    /**
                     * 为云端对象添加自定义数据，可直接调用，无须先调用withMetaDatas
                     * key不能为空或者""
                     *
                     */
//                    .addMetaData(new Parameter<>("key","value"))
                    /**
                     * 配置上传回调策略
                     */
//                .withPutPolicy(putPolicy)
                    /**
                     * 是否上传校验MD5
                     */
//                .withVerifyMd5(false)
                    /**
                     * 指定progress callback的间隔
                     */
//                .withProgressConfig(ProgressConfig.callbackWithPercent(10))
                    /**
                     * 配置读写流Buffer的大小, Default = 256 KB, MIN = 4 KB, MAX = 4 MB
                     */
//                    .setBufferSize(4 << 20)
                    /**
                     * 配置进度监听
                     */
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(long bytesWritten, long contentLength) {
                            JLog.D(TAG, String.format("[progress] = %d%% - [%d/%d]", (int) (bytesWritten * 1.f / contentLength * 100), bytesWritten, contentLength));
                        }
                    })
                    .execute();
            JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }

    public static void putFileAsync(File file, String mimeType, String nameAs, String toBucket) throws UfileClientException {
        /**
         * 上传回调策略
         * 必须填写回调接口url(目前仅支持http，不支持https)，可选填回调参数，回调参数请自行决定是否需要urlencode
         * 若配置上传回调，则上传接口的回调将会透传回调接口的response，包括httpCode
         */
        PutPolicy putPolicy = new PutPolicyForCallback.Builder("http://xxx.xxx.xxx.xxx[:port][/path]")
                .addCallbackBody(new PolicyParam("key", "value"))
                .build();
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .putObject(file, mimeType)
                .nameAs(nameAs)
                .toBucket(toBucket)
                /**
                 * 配置文件存储类型，分别是标准、低频、冷存，对应有效值：STANDARD | IA | ARCHIVE
                 */
                .withStorageType(StorageType.STANDARD)
                /**
                 * 为云端对象配置自定义数据，每次调用将会替换之前数据。
                 * 所有的自定义数据总大小不能超过 8KB。
                 */
//                    .withMetaDatas()
                /**
                 * 为云端对象添加自定义数据，可直接调用，无须先调用withMetaDatas
                 * key不能为空或者""
                 *
                 */
//                    .addMetaData(new Parameter<>("key","value"))
                /**
                 * 配置上传回调策略
                 */
//                .withPutPolicy(putPolicy)
                /**
                 * 是否上传校验MD5
                 */
//                .withVerifyMd5(false)
                /**
                 * 指定progress callback的间隔
                 */
//                .withProgressConfig(ProgressConfig.callbackWithPercent(10))
                /**
                 * 配置读写流Buffer的大小, Default = 256 KB, MIN = 4 KB, MAX = 4 MB
                 */
//                .setBufferSize(4 << 20)
                .executeAsync(new UfileCallback<PutObjectResultBean>() {
                    @Override
                    public void onProgress(long bytesWritten, long contentLength) {
                        JLog.D(TAG, String.format("[progress] = %d%% - [%d/%d]", (int) (bytesWritten * 1.f / contentLength * 100), bytesWritten, contentLength));
                    }

                    @Override
                    public void onResponse(PutObjectResultBean response) {
                        JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
                    }

                    @Override
                    public void onError(Request request, ApiError error, UfileErrorBean response) {
                        JLog.D(TAG, String.format("[error] = %s\n[info] = %s",
                                (error == null ? "null" : error.toString()),
                                (response == null ? "null" : response.toString())));
                    }
                });
    }

    public static void putStream(InputStream stream, String mimeType, String nameAs, String toBucket) {
        try {
            /**
             * 上传回调策略
             * 必须填写回调接口url(目前仅支持http，不支持https)，可选填回调参数，回调参数请自行决定是否需要urlencode
             * 若配置上传回调，则上传接口的回调将会透传回调接口的response，包括httpCode
             */
            PutPolicy putPolicy = new PutPolicyForCallback.Builder("http://xxx.xxx.xxx.xxx[:port][/path]")
                    .addCallbackBody(new PolicyParam("key", "value"))
                    .build();
            PutObjectResultBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .putObject(stream, mimeType)
                    .nameAs(nameAs)
                    .toBucket(toBucket)
                    /**
                     * 配置文件存储类型，分别是标准、低频、冷存，对应有效值：STANDARD | IA | ARCHIVE
                     */
                    .withStorageType(StorageType.STANDARD)
                    /**
                     * 为云端对象配置自定义数据，每次调用将会替换之前数据。
                     * 所有的自定义数据总大小不能超过 8KB。
                     */
//                    .withMetaDatas()
                    /**
                     * 为云端对象添加自定义数据，可直接调用，无须先调用withMetaDatas
                     * key不能为空或者""
                     *
                     */
//                    .addMetaData(new Parameter<>("key","value"))
                    /**
                     * 配置上传回调策略
                     */
//                .withPutPolicy(putPolicy)
                    /**
                     * 是否上传校验MD5
                     */
//                .withVerifyMd5(false)
                    /**
                     * 指定progress callback的间隔
                     */
//                .withProgressConfig(ProgressConfig.callbackWithPercent(10))
                    /**
                     * 配置读写流Buffer的大小, Default = 256 KB, MIN = 4 KB, MAX = 4 MB
                     */
//                    .setBufferSize(4 << 20)
                    /**
                     * 配置进度监听
                     */
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(long bytesWritten, long contentLength) {
                            JLog.D(TAG, String.format("[progress] = %d%% - [%d/%d]", (int) (bytesWritten * 1.f / contentLength * 100), bytesWritten, contentLength));
                        }
                    })
                    .execute();
            JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }

    public static void putStreamAsync(InputStream stream, String mimeType, String nameAs, String toBucket) throws UfileClientException {
        /**
         * 上传回调策略
         * 必须填写回调接口url(目前仅支持http，不支持https)，可选填回调参数，回调参数请自行决定是否需要urlencode
         * 若配置上传回调，则上传接口的回调将会透传回调接口的response，包括httpCode
         */
        PutPolicy putPolicy = new PutPolicyForCallback.Builder("http://xxx.xxx.xxx.xxx[:port][/path]")
                .addCallbackBody(new PolicyParam("key", "value"))
                .build();
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .putObject(stream, mimeType)
                .nameAs(nameAs)
                .toBucket(toBucket)
                /**
                 * 配置文件存储类型，分别是标准、低频、冷存，对应有效值：STANDARD | IA | ARCHIVE
                 */
                .withStorageType(StorageType.STANDARD)
                /**
                 * 为云端对象配置自定义数据，每次调用将会替换之前数据。
                 * 所有的自定义数据总大小不能超过 8KB。
                 */
//                    .withMetaDatas()
                /**
                 * 为云端对象添加自定义数据，可直接调用，无须先调用withMetaDatas
                 * key不能为空或者""
                 *
                 */
//                    .addMetaData(new Parameter<>("key","value"))
                /**
                 * 配置上传回调策略
                 */
//                .withPutPolicy(putPolicy)
                /**
                 * 是否上传校验MD5
                 */
//                .withVerifyMd5(false)
                /**
                 * 指定progress callback的间隔
                 */
//                .withProgressConfig(ProgressConfig.callbackWithPercent(10))
                /**
                 * 配置读写流Buffer的大小, Default = 256 KB, MIN = 4 KB, MAX = 4 MB
                 */
//                .setBufferSize(4 << 20)
                .executeAsync(new UfileCallback<PutObjectResultBean>() {
                    @Override
                    public void onProgress(long bytesWritten, long contentLength) {
                        JLog.D(TAG, String.format("[progress] = %d%% - [%d/%d]", (int) (bytesWritten * 1.f / contentLength * 100), bytesWritten, contentLength));
                    }

                    @Override
                    public void onResponse(PutObjectResultBean response) {
                        JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
                    }

                    @Override
                    public void onError(Request request, ApiError error, UfileErrorBean response) {
                        JLog.D(TAG, String.format("[error] = %s\n[info] = %s",
                                (error == null ? "null" : error.toString()),
                                (response == null ? "null" : response.toString())));
                    }
                });
    }

}
