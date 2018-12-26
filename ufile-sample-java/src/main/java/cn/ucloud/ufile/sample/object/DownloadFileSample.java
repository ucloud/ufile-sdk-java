package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.bean.DownloadFileBean;
import cn.ucloud.ufile.bean.ObjectProfile;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileException;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.UfileCallback;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;
import okhttp3.Request;


/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class DownloadFileSample {
    private static final String TAG = "DownloadFileSample";
    private static ObjectConfig config = new ObjectConfig("your bucket region", "ufileos.com");

    public static void main(String[] args) {
        String keyName = "which";
        String bucketName = "bucketName";
        String localDir = "local save dir";
        String saveName = "local save name";
        try {
            downloadFile(keyName, bucketName, localDir, saveName);
        } catch (UfileException e) {
            e.printStackTrace();
        }
    }

    public static void downloadFile(String keyName, String bucketName, String localDir, String saveName) throws UfileException {
        ObjectProfile profile = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .objectProfile(keyName, bucketName)
                .execute();

        JLog.D(TAG, String.format("[res] = %s", (profile == null ? "null" : profile.toString())));
        if (profile == null)
            return;

        DownloadFileBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .downloadFile(profile)
                .saveAt(localDir, saveName)
                /**
                 * 选择要下载的对象的范围，Default = (0, whole size)
                 */
//              .withinRange(0, 0)
                /**
                 * 配置同时分片下载的进程数，Default = 10
                 */
//              .together(5)
                /**
                 * 是否覆盖本地已有文件, Default = true;
                 */
//              .withCoverage(false)
                /**
                 * 指定progress callback的间隔
                 */
//              .withProgressConfig(ProgressConfig.callbackWithPercent(10))
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
    }

    public static void downloadFileAsync(String keyName, String bucketName, String localDir, String saveName) throws UfileException {
        ObjectProfile profile = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .objectProfile(keyName, bucketName)
                .execute();

        JLog.D(TAG, String.format("[res] = %s", (profile == null ? "null" : profile.toString())));
        if (profile == null)
            return;

        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .downloadFile(profile)
                .saveAt(localDir, saveName)
                /**
                 * 选择要下载的对象的范围，Default = (0, whole size)
                 */
//              .withinRange(0, 0)
                /**
                 * 配置同时分片下载的进程数，Default = 10
                 */
//              .together(5)
                /**
                 * 是否覆盖本地已有文件, Default = true;
                 */
//              .withCoverage(false)
                /**
                 * 指定progress callback的间隔
                 */
//              .withProgressConfig(ProgressConfig.callbackWithPercent(10))
                .executeAsync(new UfileCallback<DownloadFileBean>() {
                    @Override
                    public void onProgress(long bytesWritten, long contentLength) {
                        JLog.D(TAG, String.format("[progress] = %d%% - [%d/%d]", (int) (bytesWritten * 1.f / contentLength * 100), bytesWritten, contentLength));
                    }

                    @Override
                    public void onResponse(DownloadFileBean response) {
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