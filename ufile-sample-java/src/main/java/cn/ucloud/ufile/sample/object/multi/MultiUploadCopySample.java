package cn.ucloud.ufile.sample.object.multi;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.multi.MultiUploadInfo;
import cn.ucloud.ufile.api.object.multi.MultiUploadPartState;
import cn.ucloud.ufile.api.object.policy.PolicyParam;
import cn.ucloud.ufile.api.object.policy.PutPolicy;
import cn.ucloud.ufile.api.object.policy.PutPolicyForCallback;
import cn.ucloud.ufile.bean.MultiUploadResponse;
import cn.ucloud.ufile.bean.base.BaseObjectResponseBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileFileException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static cn.ucloud.ufile.UfileConstants.MULTIPART_SIZE;

/**
 * @author: kenny
 * @E-mail: kenny.wang@ucloud.cn
 * @date: 2023-06-28 10:52
 */
public class MultiUploadCopySample {
    private static final String TAG = "MultiUploadCopySample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        String filePath = "";
        File file = new File(filePath);
        String keyName = "";
        String bucketName = "";
        String sourceBucketName = "";
        String sourceObjectName = "";

        String mimeType = "";
        try {
            mimeType = MimeTypeUtil.getMimeType(file);
        } catch (UfileFileException e) {
            e.printStackTrace();
        }
        System.out.println(mimeType);

        List<Range> fileRanges = generateFileRanges(filePath);
        for (Range range : fileRanges) {
            System.out.println("Range: " + range.getStart() + ", " + range.getEnd());
        }

        // 先初始化分片上传请求
        MultiUploadInfo state = initMultiUpload(mimeType, keyName, bucketName);
        JLog.D(TAG, String.format("[init state] = %s", (state == null ? "null" : state.toString())));
        if (state == null)
            return;

        List<MultiUploadPartState> partStates = multiUploadCopy(state, sourceBucketName, sourceObjectName, fileRanges);
        // 若上传分片结果列表为空，则失败，需中断上传操作。否则完成上传
        if (partStates == null || partStates.isEmpty())
            abortMultiUpload(state);
        else
            finishMultiUpload(state, partStates);
    }

    public static List<Range> generateFileRanges(String filePath) {
        List<Range> fileRanges = new ArrayList<>();
        File file = new File(filePath);
        long fileSize = file.length();
        int numChunks = (int) Math.ceil((double) fileSize / MULTIPART_SIZE);

        long start = 0;
        long end = MULTIPART_SIZE - 1;
        for (int i = 0; i < numChunks; i++) {
            if (i == numChunks - 1) {
                // 最后一个分块可能大小小于 MULTIPART_SIZE
                end = fileSize - 1;
            }
            Range range = new Range(start, end);
            fileRanges.add(range);

            start += MULTIPART_SIZE;
            end += MULTIPART_SIZE;
        }
        return fileRanges;
    }

    public static MultiUploadInfo initMultiUpload(String mimeType, String keyName, String bucketName) {
        try {
            // MimeTypeUtil可能支持的type类型不全，用户可以按需自行填写
            return UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .initMultiUpload(keyName, mimeType, bucketName)
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
        } catch (UfileClientException | UfileServerException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<MultiUploadPartState> multiUploadCopy(MultiUploadInfo state, String sbn, String son, List<Range> fileRanges) {
        List<MultiUploadPartState> partStates = null;
        partStates = new ArrayList<>();
        for (int i = 0; i < fileRanges.size(); i++) {
            Range fileRange = fileRanges.get(i);
            int uploadCount = 0;
            int tryCount = 1;
            while (uploadCount < tryCount) {
                try {
                    MultiUploadPartState partState = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                            .multiUploadCopyPart(state, i, sbn, son, fileRange.getStart(), fileRange.getEnd())
                            .execute();
                    if (partState == null) {
                        uploadCount++;
                        continue;
                    }

                    partStates.add(partState);
                    break;
                } catch (UfileClientException | UfileServerException e) {
                    e.printStackTrace();
                    // 尝试次数+1
                    uploadCount++;
                }
            }
            if (uploadCount == tryCount)
                return null;
        }
        return partStates;
    }

    public static void abortMultiUpload(MultiUploadInfo info) {
        try {
            BaseObjectResponseBean abortRes = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .abortMultiUpload(info)
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


class Range {
    private long start;
    private long end;

    public Range(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}
