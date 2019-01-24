package cn.ucloud.ufile.http.request.body;

import cn.ucloud.ufile.UfileConstants;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.ProgressConfig;
import cn.ucloud.ufile.util.FileUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Source;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 上传请求体，用于拦截上传进度进行回调
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/9 15:04
 */
public abstract class UploadRequestBody<T> extends RequestBody {
    /**
     * 上传流写入的最大Buffer大小：4 MB
     */
    public static final int MAX_BUFFER_SIZE = 4 << 20;
    /**
     * 上传流写入的最小Buffer大小：4 KB
     */
    public static final int MIN_BUFFER_SIZE = 4 << 10;

    /**
     * 上传的内容，泛型指定
     */
    protected T content;
    /**
     * 上传内容的长度
     */
    protected long contentLength = 0;
    /**
     * 上传内容的MIME类型(Content-Type)
     */
    protected MediaType contentType;
    /**
     * 进度回调监听
     */
    protected OnProgressListener onProgressListener;
    /**
     * 进度回调设置
     */
    protected ProgressConfig progressConfig;

    /**
     * 流写入的buffer大小，Default = 256 KB
     */
    protected long bufferSize = UfileConstants.DEFAULT_BUFFER_SIZE;

    /**
     * 已写入的大小
     */
    protected AtomicLong bytesWritten;
    /**
     * 已写入的大小的缓存，用于用户根据分片大小回调进度
     */
    protected AtomicLong bytesWrittenCache;

    /**
     * 进度计时器，用户用户根据时间间隔回调进度
     */
    protected Timer progressTimer;
    protected ProgressTask progressTask;

    /**
     * 进度TimerTask，用户用户根据时间间隔回调进度
     */
    protected class ProgressTask extends TimerTask {
        private long totalSize = 0l;

        protected ProgressTask(long totalSize) {
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

    public UploadRequestBody() {
    }

    /**
     * 构造方法
     *
     * @param content            上传的内容
     * @param contentType        上传内容的MIME类型(Content-Type)
     * @param onProgressListener 进度回调监听
     */
    public UploadRequestBody(T content, MediaType contentType, OnProgressListener onProgressListener) {
        this.content = content;
        this.contentType = contentType;
        this.onProgressListener = onProgressListener;
        this.progressConfig = ProgressConfig.callbackDefault();
    }

    public UploadRequestBody<T> setProgressConfig(ProgressConfig progressConfig) {
        this.progressConfig = progressConfig == null ? this.progressConfig : progressConfig;
        return this;
    }

    public abstract UploadRequestBody setContent(T content);

    public UploadRequestBody setContentType(MediaType contentType) {
        this.contentType = contentType;
        return this;
    }

    public UploadRequestBody setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
        return this;
    }

    public UploadRequestBody setBufferSize(long bufferSize) {
        this.bufferSize = Math.max(MIN_BUFFER_SIZE, Math.min(bufferSize, MAX_BUFFER_SIZE));
        return this;
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    public UploadRequestBody<T> setContentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    /**
     * 执行数据流写入
     *
     * @param sink   buffer
     * @param source 源
     * @throws IOException IO异常时抛出
     */
    protected void doWriteTo(BufferedSink sink, Source source) throws IOException {
        long read;

        bytesWritten = new AtomicLong(0l);
        bytesWrittenCache = new AtomicLong(0l);

        if (onProgressListener != null)
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

        try {
            while ((read = source.read(sink.buffer(), bufferSize)) > 0) {
                sink.flush();
                if (onProgressListener == null)
                    continue;

                long written = bytesWritten.addAndGet(read);
                long cache = bytesWrittenCache.addAndGet(read);

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
            FileUtil.close(source);
        }
    }
}
