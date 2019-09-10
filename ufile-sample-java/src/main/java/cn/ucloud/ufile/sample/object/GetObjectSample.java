package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.auth.UfileAuthorizationException;
import cn.ucloud.ufile.auth.sign.UfileSignatureException;
import cn.ucloud.ufile.bean.DownloadFileBean;
import cn.ucloud.ufile.bean.DownloadStreamBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.UfileCallback;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;
import okhttp3.Request;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;


/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class GetObjectSample {
    private static final String TAG = "GetObjectSample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        String keyName = "";
        String bucketName = "";
        //  5 * 60秒 --> 5分钟后过期
        int expiresDuration = 5 * 60;

        String localDir = "";
        String saveName = "";
        try {
            String url = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .getDownloadUrlFromPrivateBucket(keyName, bucketName, expiresDuration)
                    .createUrl();
            getStream(url, localDir, saveName);
        } catch (UfileParamException e) {
            e.printStackTrace();
        } catch (UfileAuthorizationException e) {
            e.printStackTrace();
        } catch (UfileSignatureException e) {
            e.printStackTrace();
        } catch (UfileClientException e) {
            e.printStackTrace();
        }
    }

    public static void getFile(String url, String localDir, String saveName) {
        try {
            DownloadFileBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .getFile(url)
                    .saveAt(localDir, saveName)
                    /**
                     * 是否覆盖本地已有文件, Default = true;
                     */
//                .withCoverage(false)
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

    public static void getFileAsync(String url, String localDir, String saveName) {
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .getFile(url)
                .saveAt(localDir, saveName)
                /**
                 * 是否覆盖本地已有文件, Default = true;
                 */
//                .withCoverage(false)
                /**
                 * 指定progress callback的间隔
                 */
//                .withProgressConfig(ProgressConfig.callbackWithPercent(10))
                /**
                 * 配置读写流Buffer的大小, Default = 256 KB, MIN = 4 KB, MAX = 4 MB
                 */
//                    .setBufferSize(4 << 20)
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

    public static void getStream(String url, String localDir, String saveName) {
        try {
            OutputStream os = null;
            os = new FileOutputStream(new File(localDir, saveName));

            DownloadStreamBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .getStream(url)
                    /**
                     * 重定向流
                     *
                     * 默认不重定向流，下载的流会以InputStream的形式在Response中回调，并且不会回调下载进度 onProgress;
                     *
                     * 如果配置了重定向的输出流，则Response {@link DownloadStreamBean}的 InputStream = null,
                     * 因为流已被重定向导流到OutputStream，并且会回调进度 onProgress。
                     */
                    .redirectStream(os)
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
        } catch (UfileServerException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (UfileClientException e1) {
            e1.printStackTrace();
        }
    }

    public static void getStreamAsync(String url, String localDir, String saveName) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(new File(localDir, saveName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .getStream(url)
                /**
                 * 重定向流
                 *
                 * 默认不重定向流，下载的流会以InputStream的形式在Response中回调，并且不会回调下载进度 onProgress;
                 *
                 * 如果配置了重定向的输出流，则Response {@link DownloadStreamBean}的 InputStream = null,
                 * 因为流已被重定向导流到OutputStream，并且会回调进度 onProgress。
                 */
                .redirectStream(os)
                /**
                 * 指定progress callback的间隔
                 */
//                .withProgressConfig(ProgressConfig.callbackWithPercent(10))
                /**
                 * 配置读写流Buffer的大小, Default = 256 KB, MIN = 4 KB, MAX = 4 MB
                 */
//                    .setBufferSize(4 << 20)
                .executeAsync(new UfileCallback<DownloadStreamBean>() {
                    @Override
                    public void onProgress(long bytesWritten, long contentLength) {
                        JLog.D(TAG, String.format("[progress] = %d%% - [%d/%d]", (int) (bytesWritten * 1.f / contentLength * 100), bytesWritten, contentLength));
                    }

                    @Override
                    public void onResponse(DownloadStreamBean response) {
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
