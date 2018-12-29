package cn.ucloud.ufile.sample.object.multi;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.multi.MultiUploadPartState;
import cn.ucloud.ufile.api.object.multi.MultiUploadInfo;
import cn.ucloud.ufile.bean.MultiUploadResponse;
import cn.ucloud.ufile.bean.base.BaseResponseBean;
import cn.ucloud.ufile.exception.UfileException;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.ProgressConfig;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.FileUtil;
import cn.ucloud.ufile.util.JLog;

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
    private static ObjectConfig config = new ObjectConfig("your bucket region", "ufileos.com");

    public static void main(String[] args) {
        File file = new File("");
        String keyName = "";
        String mimeType = "";
        String bucketName = "";

        try {
            MultiUploadInfo state = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .initMultiUpload(keyName, mimeType, bucketName)
                    .execute();

            JLog.D(TAG, String.format("[init state] = %s", (state == null ? "null" : state.toString())));
            multiUpload(file, state);
        } catch (UfileException e) {
            e.printStackTrace();
        }
    }

    public static void multiUpload(File file, MultiUploadInfo state) {
        byte[] buffer = new byte[state.getBlkSize()];
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            int len = 0;
            int count = 0;

            ExecutorService threadPool = Executors.newFixedThreadPool(5);
            List<MultiUploadPartCallable> callables = new ArrayList<>();

            // 将数据根据state中指定的大小进行分片
            while ((len = is.read(buffer)) > 0)
                callables.add(new MultiUploadPartCallable(state, count++, Arrays.copyOf(buffer, len)));

            List<Future<MultiUploadPartState>> futures = threadPool.invokeAll(callables);

            List<MultiUploadPartState> partStates = new ArrayList<>();
            try {
                for (Future<MultiUploadPartState> future : futures)
                    partStates.add(future.get());

                MultiUploadResponse finishRes = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                        .finishMultiUpload(state, partStates)
                        .execute();
                JLog.D(TAG, "finish->" + finishRes.toString());
            } catch (ExecutionException e) {
                e.printStackTrace();
                // 若上传任一分片未成功，则须执行中断上传，然后从init开始从新上传
                BaseResponseBean abortRes = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                        .abortMultiUpload(state)
                        .execute();
                JLog.D(TAG, "abort->" + abortRes.toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UfileException e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(is);
        }
    }

    private static class MultiUploadPartCallable implements Callable<MultiUploadPartState> {
        private MultiUploadInfo state;
        private int index;
        private byte[] data;

        public MultiUploadPartCallable(MultiUploadInfo state, int index, byte[] data) {
            this.state = state;
            this.index = index;
            this.data = data;
        }

        @Override
        public MultiUploadPartState call() throws Exception {
            return UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .multiUploadPart(state, data, index)
                    /**
                     * 指定progress callback的间隔
                     */
//                    .withProgressConfig(ProgressConfig.callbackWithPercent(50))
                    /**
                     * 配置进度监听
                     */
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(long bytesWritten, long contentLength) {
                            JLog.D(TAG, String.format("[index] = %d\t[progress] = %d%% - [%d/%d]", index, (int) (bytesWritten * 1.f / contentLength * 100), bytesWritten, contentLength));
                        }
                    })
                    .execute();
        }
    }
}
