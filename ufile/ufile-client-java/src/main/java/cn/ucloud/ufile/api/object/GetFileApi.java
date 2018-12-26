package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.bean.DownloadFileBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.BaseHttpCallback;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.ProgressConfig;
import cn.ucloud.ufile.http.request.GetRequestBuilder;
import cn.ucloud.ufile.util.FileUtil;
import cn.ucloud.ufile.util.ParameterValidator;
import cn.ucloud.ufile.UfileConstants;
import okhttp3.Response;
import sun.security.validator.ValidatorException;

import javax.validation.constraints.NotEmpty;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * API-Get下载小文件
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:07
 */
public class GetFileApi extends UfileObjectApi<DownloadFileBean> {
    /**
     * Required
     * 下载文件保存的本地目录路径
     */
    @NotEmpty(message = "Param 'localPath' is required")
    private String localPath;
    /**
     * Required
     * 下载文件保存的本地文件名
     */
    @NotEmpty(message = "Param 'saveName' is required")
    private String saveName;

    /**
     * Optional
     * 是否覆盖本地已有文件，Default = true
     */
    private boolean isCover = true;
    private ProgressConfig progressConfig;
    private AtomicLong bytesWritten;
    private AtomicLong bytesWrittenCache;

    /**
     * 构造方法
     *
     * @param authorizer Object授权器
     * @param host       API域名
     * @param httpClient Http客户端
     */
    protected GetFileApi(ObjectAuthorizer authorizer, String host, HttpClient httpClient) {
        super(authorizer, host, httpClient);
        progressConfig = ProgressConfig.callbackDefault();
    }

    /**
     * 配置下载后的保存目录和文件名
     *
     * @param localPath 保存目录
     * @param saveName  保存文件名
     * @return {@link GetFileApi}
     */
    public GetFileApi saveAt(String localPath, String saveName) {
        this.localPath = localPath;
        this.saveName = saveName;
        return this;
    }

    /**
     * 配置若本地已存在文件是否覆盖
     *
     * @param isCover 是否覆盖
     * @return {@link GetFileApi}
     */
    public GetFileApi withCoverage(boolean isCover) {
        this.isCover = isCover;
        return this;
    }

    /**
     * 配置进度回调设置
     *
     * @param config 进度回调设置
     * @return {@link GetFileApi}
     */
    public GetFileApi withProgressConfig(ProgressConfig config) {
        progressConfig = config == null ? this.progressConfig : config;
        return this;
    }

    @Override
    protected void prepareData() throws UfileException {
        if (host == null || host.length() == 0)
            throw new UfileRequiredParamNotFoundException("Param 'host' is null!");

        try {
            ParameterValidator.validator(this);
            bytesWritten = new AtomicLong(0);
            bytesWrittenCache = new AtomicLong(0);
            call = new GetRequestBuilder()
                    .baseUrl(host)
                    .build(httpClient.getOkHttpClient());
        } catch (ValidatorException e) {
            throw new UfileRequiredParamNotFoundException(e);
        }
    }

    private Timer progressTimer;
    private ProgressTask progressTask;

    private class ProgressTask extends TimerTask {
        private long totalSize = 0l;

        private ProgressTask(long totalSize) {
            this.totalSize = totalSize;
        }

        @Override
        public void run() {
            if (onProgressListener != null) {
                synchronized (bytesWritten) {
                    onProgressListener.onProgress(bytesWritten.get(), totalSize);
                }
            }
        }
    }

    private OnProgressListener onProgressListener;

    /**
     * 配置进度监听器
     * 该配置可供execute()同步接口回调进度使用，若使用executeAsync({@link BaseHttpCallback})，则后配置的会覆盖新配置的
     *
     * @param onProgressListener 进度监听器
     * @return {@link GetFileApi}
     */
    public GetFileApi setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
        return this;
    }

    @Override
    public void executeAsync(BaseHttpCallback<DownloadFileBean, UfileErrorBean> callback) {
        onProgressListener = callback;
        super.executeAsync(callback);
    }

    @Override
    public DownloadFileBean parseHttpResponse(Response response) throws IOException {
        DownloadFileBean result = new DownloadFileBean();
        long contentLength = response.body().contentLength();
        result.setContentLength(contentLength);
        result.seteTag(response.header("ETag").replace("\"", ""));

        if (onProgressListener != null) {
            switch (progressConfig.type) {
                case PROGRESS_INTERVAL_TIME: {
                    // progressIntervalType是按时间周期回调，则自动做 0 ~ progressInterval 的合法化赋值，progressInterval置0，即实时回调读写进度
                    progressConfig.interval = Math.max(0, progressConfig.interval);
                    progressTimer = new Timer();
                    progressTask = new ProgressTask(contentLength);
                    progressTimer.scheduleAtFixedRate(progressTask, progressConfig.interval, progressConfig.interval);
                    break;
                }
                case PROGRESS_INTERVAL_PERCENT: {
                    // progressIntervalType是按百分比回调，则若progressInterval<0 | >100，progressInterval置0，即实时回调读写进度
                    if (progressConfig.interval < 0 || progressConfig.interval > 100)
                        progressConfig.interval = 0l;
                    else
                        progressConfig.interval = (long) (progressConfig.interval / 100.f * contentLength);
                    break;
                }
                case PROGRESS_INTERVAL_BUFFER: {
                    // progressIntervalType是按读写的buffer size回调，则自动做 0 ~ totalSize-1 的合法化赋值，progressInterval置0，即实时回调读写进度
                    progressConfig.interval = Math.max(0, Math.min(contentLength - 1, progressConfig.interval));
                    break;
                }
            }
        }

        File dir = new File(localPath);
        if (!dir.exists() || (dir.exists() && !dir.isDirectory()))
            dir.mkdirs();

        String absPath = localPath + (localPath.endsWith(File.separator) ? "" : File.separator) + saveName;

        File file = new File(absPath);

        if (file.exists() && file.isFile())
            if (isCover) {
                FileUtil.deleteFileCleanly(file);
                file = new File(absPath);
            } else {
                int i = 1;
                boolean isExist = true;
                while (isExist) {
                    String tmpPath = absPath + String.format("-%d", i++);
                    file = new File(tmpPath);
                    if (!file.exists() || file.isDirectory()) {
                        isExist = false;
                        absPath = tmpPath;
                    }
                }
            }

        InputStream is = response.body().byteStream();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[UfileConstants.DEFAULT_BUFFER_SIZE];
            int len = 0;

            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);

                if (onProgressListener == null)
                    continue;

                long written = bytesWritten.addAndGet(len);
                long cache = bytesWrittenCache.addAndGet(len);

                if (progressConfig.type == ProgressConfig.ProgressIntervalType.PROGRESS_INTERVAL_TIME)
                    continue;

                if (written < contentLength && cache < progressConfig.interval)
                    continue;

                bytesWrittenCache.set(0);
                onProgressListener.onProgress(written, contentLength);
            }
        } finally {
            if (progressConfig.type == ProgressConfig.ProgressIntervalType.PROGRESS_INTERVAL_TIME) {
                if (progressTask != null)
                    progressTask.cancel();
                if (progressTimer != null)
                    progressTimer.cancel();

                if (onProgressListener != null)
                    synchronized (bytesWritten) {
                        onProgressListener.onProgress(bytesWritten.get(), contentLength);
                    }
            }
            FileUtil.close(fos, is);
        }

        result.setFile(file);

        return result;
    }
}
