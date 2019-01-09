package cn.ucloud.ufile.sample.object.multi;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.multi.MultiUploadPartState;
import cn.ucloud.ufile.api.object.multi.MultiUploadInfo;
import cn.ucloud.ufile.bean.MultiUploadResponse;
import cn.ucloud.ufile.bean.base.BaseResponseBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.FileUtil;
import cn.ucloud.ufile.util.JLog;
import cn.ucloud.ufile.util.MimeTypeUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class MultiUploadSample {
    private static final String TAG = "MultiUploadSample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        File file = new File("/Users/joshua/Downloads/testDir/googlechrome.dmg");
        String keyName = file.getName();
        String bucketName = "new-bucket";

        // 先初始化分片上环请求
        MultiUploadInfo state = initMultiUpload(file, keyName, bucketName);
        if (state == null)
            return;

        JLog.D(TAG, String.format("[init state] = %s", (state == null ? "null" : state.toString())));

        List<MultiUploadPartState> partStates = multiUpload(file, state);
        // 若上传分片结果列表为空，则失败，需中断上传操作。否则完成上传
        if (partStates == null || partStates.isEmpty())
            abortMultiUpload(state);
        else
            finishMultiUpload(state, partStates);
    }

    public static MultiUploadInfo initMultiUpload(File file, String keyName, String bucketName) {
        try {
            // MimeTypeUtil可能支持的type类型不全，用户可以按需自行填写
            String mimeType = MimeTypeUtil.getMimeType(file);
            return UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .initMultiUpload(keyName, mimeType, bucketName)
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
                                .multiUploadPart(state, buffer, index)
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
            BaseResponseBean abortRes = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
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
            MultiUploadResponse res = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .finishMultiUpload(state, partStates)
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
