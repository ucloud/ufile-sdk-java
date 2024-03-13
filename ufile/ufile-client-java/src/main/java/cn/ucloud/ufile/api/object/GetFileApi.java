package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.bean.DownloadFileBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileIOException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.BaseHttpCallback;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.ProgressConfig;
import cn.ucloud.ufile.http.request.GetRequestBuilder;
import cn.ucloud.ufile.util.FileUtil;
import cn.ucloud.ufile.UfileConstants;
import cn.ucloud.ufile.util.Parameter;
import okhttp3.Response;

import java.io.*;
import java.util.*;
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
    private String localPath;
    /**
     * Required
     * 下载文件保存的本地文件名
     */
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
     * Optional
     * 要下载的对象的范围起始偏移量，Default = 0，若0则从0开始下载
     */
    private long rangeStart;
    /**
     * Optional
     * 要下载的对象的范围长度，Default = 0，若0则下载整个对象
     */
    private long rangeEnd;
    /**
     * 流读取的buffer大小，Default = 256 KB
     */
    private int bufferSize = UfileConstants.DEFAULT_BUFFER_SIZE;

    /**
     * 构造方法
     *
     * @param authorizer   Object授权器
     * @param objectConfig ObjectConfig {@link ObjectConfig}
     * @param httpClient   Http客户端
     */
    protected GetFileApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
        RESP_CODE_SUCCESS = 200;
        host = objectConfig.getCustomHost();
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
     * 选择要下载的对象的范围，Default = [0, whole size]
     *
     * @param start range起点
     * @param end   range终点
     * @return {@link GetFileApi}
     */
    public GetFileApi withinRange(long start, long end) {
        this.rangeStart = start;
        this.rangeEnd = end;
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

    /**
     * 设置流读写的Buffer大小，默认 256 KB
     *
     * @param bufferSize Buffer大小
     * @return {@link GetFileApi}
     */
    public GetFileApi setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    @Override
    protected void prepareData() throws UfileParamException {
        parameterValidat();
        bytesWritten = new AtomicLong(0);
        bytesWrittenCache = new AtomicLong(0);
        call = new GetRequestBuilder()
                .setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut)
                .baseUrl(host)
                .header(headers)
                .addHeader("Range", String.format("bytes=%d-%s", rangeStart, rangeEnd == 0 ? "" : rangeEnd))
                .build(httpClient.getOkHttpClient());
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (host == null || host.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'url' can not be null or empty");

        if (localPath == null || localPath.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'localPath' can not be null or empty");

        if (saveName == null || saveName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'saveName' can not be null or empty");

        if (rangeStart < 0l)
            throw new UfileParamException("Invalid range param 'start', start must be >= 0");
        if (rangeEnd < 0l)
            throw new UfileParamException("Invalid range param 'end', end must be >= 0");
        if (rangeEnd > 0 && rangeEnd <= rangeStart)
            throw new UfileParamException("Invalid range, end must be > start");
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
    public DownloadFileBean parseHttpResponse(Response response) throws UfileIOException {
        InputStream is = null;
        FileOutputStream fos = null;
        DownloadFileBean result = new DownloadFileBean();
        long contentLength = response.body().contentLength();
        try {
            result.setContentLength(contentLength);
            result.setContentType(response.header("Content-Type"));
            result.seteTag(response.header("ETag") == null ?
                    null : response.header("ETag").replace("\"", ""));

            if (response.headers() != null) {
                Set<String> names = response.headers().names();
                if (names != null) {
                    Map<String, String> headers = new HashMap<>();
                    Map<String, String> metadata = new HashMap<>();
                    for (String name : names) {
                        headers.put(name, response.header(name, null));
                        if (name == null || !name.startsWith("X-Ufile-Meta-"))
                            continue;

                        String key = name.substring(13).toLowerCase();
                        metadata.put(key, response.header(name, ""));
                    }
                    result.setHeaders(headers);
                    result.setMetadatas(metadata);
                }
            }

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

            is = response.body().byteStream();
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[(int) bufferSize];
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

            result.setFile(file);
        } catch (IOException e) {
            throw new UfileIOException("Occur IOException while IO stream");
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
            FileUtil.close(fos, is, response.body());
        }

        return result;
    }
}
