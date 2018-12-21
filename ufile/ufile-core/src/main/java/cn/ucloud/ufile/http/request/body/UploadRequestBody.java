package cn.ucloud.ufile.http.request.body;

import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.ProgressConfig;
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
    protected OnProgressListener uploadListener;
    /**
     * 进度回调设置
     */
    protected ProgressConfig progressConfig;

    /**
     * 流写入的buffer大小，Default = 32 KB
     */
    protected long bufferSize = 32 << 10;

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
            if (uploadListener != null) {
                synchronized (bytesWritten) {
                    uploadListener.onProgress(bytesWritten.get(), totalSize);
                }
            }
        }
    }

    /**
     * 构造方法
     *
     * @param content        上传的内容
     * @param contentType    上传内容的MIME类型(Content-Type)
     * @param uploadListener 进度回调监听
     */
    public UploadRequestBody(T content, MediaType contentType, OnProgressListener uploadListener) {
        this.content = content;
        this.contentType = contentType;
        this.uploadListener = uploadListener;
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

    public UploadRequestBody setUploadListener(OnProgressListener uploadListener) {
        this.uploadListener = uploadListener;
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

        if (uploadListener != null)
            switch (progressConfig.type) {
                case PROGRESS_INTERVAL_TIME: {
                    // progressIntervalType是按时间周期回调，则自动做 0 ~ progressInterval 的合法化赋值，progressInterval置0，即实时回调读写进度
                    progressConfig.interval = Math.max(0, progressConfig.interval);
                    progressTimer = new Timer();
                    progressTimer.scheduleAtFixedRate(new ProgressTask(contentLength), progressConfig.interval, progressConfig.interval);
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
                if (uploadListener == null)
                    continue;

                long written = bytesWritten.addAndGet(read);
                long cache = bytesWrittenCache.addAndGet(read);
                synchronized (bytesWritten) {
                    if (written < contentLength && cache < progressConfig.interval)
                        continue;

                    if (progressConfig.type != ProgressConfig.ProgressIntervalType.PROGRESS_INTERVAL_TIME) {
                        bytesWrittenCache.set(0);
                        uploadListener.onProgress(written, contentLength);
                    } else {
                        if (written >= contentLength) {
                            progressTimer.cancel();
                            uploadListener.onProgress(written, contentLength);
                        }
                    }
                }
            }
        } finally {
            if (source != null)
                source.close();
        }
    }
}
