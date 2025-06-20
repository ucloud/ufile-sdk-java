package cn.ucloud.ufile.sample.object.multi;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.multi.MultiUploadPartState;
import cn.ucloud.ufile.api.object.multi.MultiUploadInfo;
import cn.ucloud.ufile.api.object.policy.PolicyParam;
import cn.ucloud.ufile.api.object.policy.PutPolicy;
import cn.ucloud.ufile.api.object.policy.PutPolicyForCallback;
import cn.ucloud.ufile.bean.MultiUploadResponse;
import cn.ucloud.ufile.bean.base.BaseObjectResponseBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class MultiUploadSample {
    private static final String TAG = "MultiUploadSample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        File file = new File("");
        String keyName = file.getName();
        String bucketName = "";

        // 先初始化分片上传请求
        MultiUploadInfo state = initMultiUpload(file, keyName, bucketName);
        JLog.D(TAG, String.format("[init state] = %s", (state == null ? "null" : state.toString())));
        if (state == null)
            return;

        List<MultiUploadPartState> partStates = multiUpload(file, state);
        // 若上传分片结果列表为空，则失败，需中断上传操作。否则完成上传
        if (partStates == null || partStates.isEmpty())
            abortMultiUpload(state);
        else
            finishMultiUpload(state, partStates);
    }

    public static MultiUploadInfo initMultiUpload(File file, String keyName, String bucketName) {
        try {
            String securityToken = Constants.SECURITY_TOKEN;
            // MimeTypeUtil可能支持的type类型不全，用户可以按需自行填写
            String mimeType = MimeTypeUtil.getMimeType(file);
            return UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .initMultiUpload(keyName, mimeType, bucketName)
                    /**
                     * 使用安全令牌
                     */
                    .withSecurityToken(securityToken)
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
                    .execute();
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<MultiUploadPartState> multiUpload(File file, MultiUploadInfo state) {
        List<MultiUploadPartState> partStates = null;
        String securityToken = Constants.SECURITY_TOKEN;
        byte[] buffer = new byte[state.getBlkSize()];
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            int len = 0;
            int count = 0;

            partStates = new ArrayList<>();
            // 将数据根据state中指定的大小进行分片
            while ((len = is.read(buffer)) > 0) {
                final int index = count++;
                byte[] sendData = Arrays.copyOf(buffer, len);
                int uploadCount = 0;

                // 可支持重试3次上传
                while (uploadCount < 3) {
                    try {
                        MultiUploadPartState partState = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                                .multiUploadPart(state, sendData, index)
                                /**
                                 * 使用安全令牌
                                 */
                                .withSecurityToken(securityToken)
                                /**
                                 * 指定progress callback的间隔
                                 */
//                                .withProgressConfig(ProgressConfig.callbackWithPercent(50))
                                /**
                                 * 配置进度监听
                                 */
                                .setOnProgressListener(new OnProgressListener() {
                                    @Override
                                    public void onProgress(long bytesWritten, long contentLength) {
                                        JLog.D(TAG, String.format("[index] = %d\t[progress] = %d%% - [%d/%d]", index,
                                                (int) (bytesWritten * 1.f / contentLength * 100), bytesWritten, contentLength));
                                    }
                                })
                                .execute();
                        if (partState == null) {
                            uploadCount++;
                            continue;
                        }

                        partStates.add(partState);
                        break;
                    } catch (UfileClientException e) {
                        e.printStackTrace();
                        // 尝试次数+1
                        uploadCount++;
                    } catch (UfileServerException e) {
                        e.printStackTrace();
                        // 尝试次数+1
                        uploadCount++;
                    }
                }

                if (uploadCount == 3)
                    return null;
            }

            return partStates;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(is);
        }

        return null;
    }

    public static void abortMultiUpload(MultiUploadInfo info) {
        try {
            String securityToken = Constants.SECURITY_TOKEN;
            BaseObjectResponseBean abortRes = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .abortMultiUpload(info)
                    /**
                     * 使用安全令牌
                     */
                    .withSecurityToken(securityToken)
                    .execute();
            JLog.D(TAG, "abort->" + abortRes.toString());
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }

    public static MultiUploadResponse finishMultiUpload(MultiUploadInfo state, List<MultiUploadPartState> partStates) {
        try {
            String securityToken = Constants.SECURITY_TOKEN;
            /**
             * 上传回调策略
             * 必须填写回调接口url(目前仅支持http，不支持https)，可选填回调参数，回调参数请自行决定是否需要urlencode。
             * 若配置上传回调，则上传接口的回调将会透传回调接口的response，包括httpCode
             */
            PutPolicy putPolicy = new PutPolicyForCallback.Builder("http://xxx.xxx.xxx.xxx[:port][/path]")
                    .addCallbackBody(new PolicyParam("key", "value"))
                    .build();
            MultiUploadResponse res = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .finishMultiUpload(state, partStates)
                    /**
                     * 使用安全令牌
                     */
                    .withSecurityToken(securityToken)
                    /**
                     * 配置上传回调策略
                     */
//                .withPutPolicy(putPolicy)
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
                     * 配置用户自定义元数据设置方式
                     * 具体参数配置可见 {@link MetadataDirective}
                     */
//                    .withMetadataDirective(MetadataDirective.UNCHANGED)
                    .execute();
            JLog.D(TAG, "finish->" + res.toString());
            return res;
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
        return null;
    }

}
