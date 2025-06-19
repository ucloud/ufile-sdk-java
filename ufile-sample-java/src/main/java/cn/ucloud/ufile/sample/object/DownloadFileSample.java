package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.bean.DownloadFileBean;
import cn.ucloud.ufile.bean.ObjectProfile;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
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
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        String keyName = "";
        String bucketName = "";
        String localDir = "";
        String saveName = "";
        downloadFile(keyName, bucketName, localDir, saveName);
    }

    public static void downloadFile(String keyName, String bucketName, String localDir, String saveName) {
        try {
            ObjectProfile profile = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .objectProfile(keyName, bucketName)
                    .execute();

            JLog.D(TAG, String.format("[res] = %s", (profile == null ? "null" : profile.toString())));
            if (profile == null)
                return;
            String securityToken = Constants.SECURITY_TOKEN;
            DownloadFileBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .downloadFile(profile)
                    .saveAt(localDir, saveName)
                    /**
                     * 使用安全令牌
                     */
              .withSecurityToken(securityToken)
                    /**
                     * 选择要下载的对象的范围，Default = [0, whole size]
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

    public static void downloadFileAsync(String keyName, String bucketName, final String localDir, final String saveName) {
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .objectProfile(keyName, bucketName)
                .executeAsync(new UfileCallback<ObjectProfile>() {
                    @Override
                    public void onResponse(ObjectProfile profile) {
                        JLog.D(TAG, String.format("[res] = %s", (profile == null ? "null" : profile.toString())));
                        if (profile == null)
                            return;

                        String securityToken = Constants.SECURITY_TOKEN;
                        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                                .downloadFile(profile)
                                .saveAt(localDir, saveName)
                                /**
                                 * 使用安全令牌
                                 */
                                .withSecurityToken(securityToken)
                                /**
                                 * 选择要下载的对象的范围，Default = (0, whole size)
                                 */
//                                .withinRange(0, 0)
                                /**
                                 * 配置同时分片下载的进程数，Default = 10
                                 */
//                                .together(5)
                                /**
                                 * 是否覆盖本地已有文件, Default = true;
                                 */
//                                .withCoverage(false)
                                /**
                                 * 指定progress callback的间隔
                                 */
//                                .withProgressConfig(ProgressConfig.callbackWithPercent(10))
                                /**
                                 * 配置读写流Buffer的大小, Default = 256 KB, MIN = 4 KB, MAX = 4 MB
                                 */
//                                .setBufferSize(4 << 20)
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

                    @Override
                    public void onError(Request request, ApiError error, UfileErrorBean response) {
                        JLog.D(TAG, String.format("[error] = %s\n[info] = %s",
                                (error == null ? "null" : error.toString()),
                                (response == null ? "null" : response.toString())));
                    }
                });
    }

}