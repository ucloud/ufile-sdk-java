package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.bean.PutObjectResultBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileFileException;
import cn.ucloud.ufile.http.UfileCallback;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;
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
//        InputStream is = new ByteArrayInputStream(new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07});
        File file = new File("/Users/joshua/Downloads/testDir/WechatIMG897.png");
        String mimeType = "image/png";
        String keyName = "WechatIMG897.png";
        String bucketName = "new-bucket";
        InputStream is = null;
        JLog.SHOW_DEBUG = true;
        JLog.SHOW_TEST = true;
        try {
            is = new FileInputStream(file);
            putStream(is, mimeType, keyName, bucketName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void putFile(File file, String mimeType, String nameAs, String toBucket) {
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .putObject(file, mimeType)
                .nameAs(nameAs)
                .toBucket(toBucket)
                /**
                 * 是否上传校验MD5
                 */
//                .withVerifyMd5(false)
                /**
                 * 指定progress callback的间隔
                 */
//                .withProgressConfig(ProgressConfig.callbackWithPercent(10))
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
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .putObject(stream, mimeType)
                .nameAs(nameAs)
                .toBucket(toBucket)
                /**
                 * 是否上传校验MD5
                 */
//                .withVerifyMd5(false)
                /**
                 * 指定progress callback的间隔
                 */
//                .withProgressConfig(ProgressConfig.callbackWithPercent(10))
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
