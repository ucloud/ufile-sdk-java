package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.UfileConstants;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.bean.DownloadFileBean;
import cn.ucloud.ufile.bean.ObjectProfile;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileIOException;
import cn.ucloud.ufile.exception.UfileParamException;
import cn.ucloud.ufile.exception.UfileRequiredParamNotFoundException;
import cn.ucloud.ufile.http.UfileHttpException;
import cn.ucloud.ufile.http.BaseHttpCallback;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.http.ProgressConfig;
import cn.ucloud.ufile.http.request.GetRequestBuilder;
import cn.ucloud.ufile.util.*;
import com.google.gson.JsonElement;
import okhttp3.Call;
import okhttp3.Response;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * API-下载文件
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/12 19:07
 */
public class DownloadFileApi extends UfileObjectApi<DownloadFileBean> {
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
     * Required
     * 要下载的云端文件描述，通过ObjectProfile API获得
     */
    private ObjectProfile profile;

    /**
     * 有效时限
     */
    private int expiresDuration = 24 * 60 * 60;
    /**
     * 并行下载线程数
     */
    private int threadCount = 10;

    /**
     * 进度回调设置
     */
    private ProgressConfig progressConfig;

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
     * Optional
     * 是否覆盖本地已有文件，Default = true
     */
    private boolean isCover = true;
    
    /**
     * Optional
     * STS 临时授权 SecurityToken
     */
    private String securityToken;

    /**
     * 下载文件的总大小
     */
    private long totalSize = 0;
    /**
     * 分片数量
     */
    private long partCount = 0;
    /**
     * 分片任务集
     */
    private List<DownloadCallable> callList;
    /**
     * 下载到本地的文件
     */
    private File finalFile;
    /**
     * 已写的Byte计数
     */
    private AtomicLong bytesWritten;
    /**
     * 已写的Byte计数缓存，用于按照Buffer大小回调进度
     */
    private AtomicLong bytesWrittenCache;
    /**
     * 下载线程池
     */
    private ExecutorService mFixedThreadPool;
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
    protected DownloadFileApi(ObjectAuthorizer authorizer, ObjectConfig objectConfig, HttpClient httpClient) {
        super(authorizer, objectConfig, httpClient);
        RESP_CODE_SUCCESS = 206;
        progressConfig = ProgressConfig.callbackDefault();
    }

    /**
     * 配置下载后的本地保存目录和文件名
     *
     * @param localPath 本地目录
     * @param saveName  保存文件名
     * @return {@link DownloadFileApi}
     */
    public DownloadFileApi saveAt(String localPath, String saveName) {
        this.localPath = localPath;
        this.saveName = saveName;
        return this;
    }

    /**
     * 配置需要下载的文件描述信息
     *
     * @param profile 文件描述信息
     * @return {@link DownloadFileApi}
     */
    public DownloadFileApi which(ObjectProfile profile) {
        this.profile = profile;
        return this;
    }

    /**
     * 选择要下载的对象的范围，Default = [0, whole size]
     *
     * @param start range起点
     * @param end   range终点
     * @return {@link DownloadFileApi}
     */
    public DownloadFileApi withinRange(long start, long end) {
        this.rangeStart = start;
        this.rangeEnd = end;
        return this;
    }

    /**
     * 配置并行下载的线程数, Default = 10
     *
     * @param threadCount 线程数
     * @return {@link DownloadFileApi}
     */
    public DownloadFileApi together(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    /**
     * 配置若本地已存在文件是否覆盖
     *
     * @param isCover 是否覆盖
     * @return {@link DownloadFileApi}
     */
    public DownloadFileApi withCoverage(boolean isCover) {
        this.isCover = isCover;
        return this;
    }

    /**
     * 配置进度回调设置
     *
     * @param config 进度回调设置
     * @return {@link DownloadFileApi}
     */
    public DownloadFileApi withProgressConfig(ProgressConfig config) {
        this.progressConfig = config == null ? this.progressConfig : config;
        return this;
    }

    /**
     * 配置签名可选参数
     *
     * @param authOptionalData 签名可选参数
     * @return {@link DownloadFileApi}
     */
    public DownloadFileApi withAuthOptionalData(JsonElement authOptionalData) {
        this.authOptionalData = authOptionalData;
        return this;
    }
    
    /**
     * 使用STS密钥签名
     *
     * @param securityToken STS token
     * @return {@link DownloadFileApi}
     */
    public DownloadFileApi withSecurityToken(String securityToken) {
        this.securityToken = securityToken;
        return this;
    }

    /**
     * 设置流读写的Buffer大小，默认 256 KB
     *
     * @param bufferSize Buffer大小
     * @return {@link DownloadFileApi}
     */
    public DownloadFileApi setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    @Override
    protected void prepareData() throws UfileClientException {
        parameterValidat();

        partCount = 0L;

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

        finalFile = file;

        if (rangeEnd == 0)
            rangeEnd = profile.getContentLength() - 1;
        totalSize = rangeEnd - rangeStart + 1;

        host = new GenerateObjectPrivateUrlApi(authorizer, objectConfig, profile.getKeyName(), profile.getBucket(), expiresDuration)
                .withAuthOptionalData(authOptionalData)
                .createUrl();

        partCount = (long) Math.ceil(totalSize * 1.d / UfileConstants.MULTIPART_SIZE);

        switch (progressConfig.type) {
            case PROGRESS_INTERVAL_TIME: {
                // progressIntervalType是按时间周期回调，则自动做 0 ~ progressInterval 的合法化赋值，progressInterval置0，即实时回调读写进度
                progressConfig.interval = Math.max(0, progressConfig.interval);
                break;
            }
            case PROGRESS_INTERVAL_PERCENT: {
                // progressIntervalType是按百分比回调，则若progressInterval<0 | >100，progressInterval置0，即实时回调读写进度
                if (progressConfig.interval < 0 || progressConfig.interval > 100)
                    progressConfig.interval = 0l;
                else
                    progressConfig.interval = (long) (progressConfig.interval / 100.f * totalSize);
                break;
            }
            case PROGRESS_INTERVAL_BUFFER: {
                // progressIntervalType是按读写的buffer size回调，则自动做 0 ~ totalSize-1 的合法化赋值，progressInterval置0，即实时回调读写进度
                progressConfig.interval = Math.max(0, Math.min(totalSize - 1, progressConfig.interval));
                break;
            }
        }

        bytesWritten = new AtomicLong(0);
        bytesWrittenCache = new AtomicLong(0);

        callList = new ArrayList<>();
        for (long i = 0L; i < partCount; i++) {
            long start = Math.max(i * UfileConstants.MULTIPART_SIZE, rangeStart);
            long end = Math.min(rangeEnd, (start + UfileConstants.MULTIPART_SIZE));
            GetRequestBuilder builder = (GetRequestBuilder) new GetRequestBuilder()
                    .baseUrl(host)
                    .header(headers)
                    .addHeader("Range", String.format("bytes=%d-%d", start, end));
            
            // Add security token if provided
            if (securityToken != null && !securityToken.isEmpty()) {
                builder.addHeader("SecurityToken", securityToken);
            }
            
            builder.setConnTimeOut(connTimeOut).setReadTimeOut(readTimeOut).setWriteTimeOut(writeTimeOut);
            callList.add(new DownloadCallable(builder.build(httpClient.getOkHttpClient()), i));
        }

        mFixedThreadPool = Executors.newFixedThreadPool(threadCount);
    }

    @Override
    protected void parameterValidat() throws UfileParamException {
        if (localPath == null || localPath.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'localPath' can not be null or empty");

        if (saveName == null || saveName.isEmpty())
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'saveName' can not be null or empty");

        if (profile == null)
            throw new UfileRequiredParamNotFoundException(
                    "The required param 'profile' can not be null");

        if (rangeStart < 0l)
            throw new UfileParamException("Invalid range param 'start', start must be >= 0");
        if (rangeEnd < 0l)
            throw new UfileParamException("Invalid range param 'end', end must be >= 0");
        if (rangeEnd > 0 && rangeEnd <= rangeStart)
            throw new UfileParamException("Invalid range, end must be > start");
        if (rangeEnd > (profile.getContentLength() - 1))
            throw new UfileParamException("Invalid range, end is out of content-length");
    }

    private OnProgressListener onProgressListener;

    /**
     * 配置进度监听器
     * 该配置可供execute()同步接口回调进度使用，若使用executeAsync({@link BaseHttpCallback})，则后配置的会覆盖新配置的
     *
     * @param onProgressListener 进度监听器
     * @return {@link DownloadFileApi}
     */
    public DownloadFileApi setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
        return this;
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

    @Override
    public DownloadFileBean execute() throws UfileClientException {
        prepareData();

        try {
            if (onProgressListener != null
                    && progressConfig.type == ProgressConfig.ProgressIntervalType.PROGRESS_INTERVAL_TIME) {
                progressTimer = new Timer();
                progressTask = new ProgressTask(totalSize);
                progressTimer.scheduleAtFixedRate(progressTask, progressConfig.interval, progressConfig.interval);
            }

            List<Future<DownloadFileBean>> futures = mFixedThreadPool.invokeAll(callList);

            if (progressConfig.type == ProgressConfig.ProgressIntervalType.PROGRESS_INTERVAL_TIME) {
                if (progressTask != null)
                    progressTask.cancel();
                if (progressTimer != null)
                    progressTimer.cancel();

                if (onProgressListener != null)
                    synchronized (bytesWritten) {
                        onProgressListener.onProgress(bytesWritten.get(), totalSize);
                    }
            }

            if (futures == null)
                throw new UfileClientException("Invoke futures are null!");

            Map<String, String> metadatas = null;
            try {
                for (int len = futures.size(), i = len - 1; i > -1; i--) {
                    Future<DownloadFileBean> future = futures.get(i);
                    if (future == null)
                        throw new UfileClientException("Invoke future is null!");
                    DownloadFileBean res = future.get();
                    if (metadatas == null && res != null && res.getMetadatas() != null)
                        metadatas = res.getMetadatas();
                }
            } catch (ExecutionException e) {
                throw new UfileClientException(e.getMessage());
            }

            Etag etag = Etag.etag(finalFile, UfileConstants.MULTIPART_SIZE);
            return new DownloadFileBean()
                    .setContentType(profile.getContentType())
                    .seteTag(etag == null ? null : etag.geteTag())
                    .setFile(finalFile)
                    .setContentLength(finalFile.length())
                    .setMetadatas(metadatas);
        } catch (IOException e) {
            throw new UfileIOException("Calculate ETag error!", e);
        } catch (InterruptedException e) {
            throw new UfileClientException("Invoke part occur error!", e);
        } finally {
            mFixedThreadPool.shutdown();
        }
    }

    @Override
    public void executeAsync(BaseHttpCallback<DownloadFileBean, UfileErrorBean> callback) {
        onProgressListener = callback;
        httpCallback = callback;

        okHttpClient.dispatcher().executorService().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    prepareData();

                    if (onProgressListener != null
                            && progressConfig.type == ProgressConfig.ProgressIntervalType.PROGRESS_INTERVAL_TIME) {
                        progressTimer = new Timer();
                        progressTask = new ProgressTask(totalSize);
                        progressTimer.scheduleAtFixedRate(progressTask, progressConfig.interval, progressConfig.interval);
                    }

                    List<Future<DownloadFileBean>> futures = mFixedThreadPool.invokeAll(callList);

                    if (progressConfig.type == ProgressConfig.ProgressIntervalType.PROGRESS_INTERVAL_TIME) {
                        if (progressTask != null)
                            progressTask.cancel();
                        if (progressTimer != null)
                            progressTimer.cancel();

                        if (onProgressListener != null)
                            synchronized (bytesWritten) {
                                onProgressListener.onProgress(bytesWritten.get(), totalSize);
                            }
                    }

                    if (futures == null)
                        throw new UfileClientException("Invoke futures are null!");

                    Map<String, String> metadatas = null;
                    try {
                        for (int len = futures.size(), i = len - 1; i > -1; i--) {
                            Future<DownloadFileBean> future = futures.get(i);
                            if (future == null)
                                throw new UfileClientException("Invoke future is null!");
                            DownloadFileBean res = future.get();
                            if (metadatas == null && res != null && res.getMetadatas() != null)
                                metadatas = res.getMetadatas();
                        }
                    } catch (ExecutionException e) {
                        throw new UfileClientException(e.getMessage());
                    }

                    if (httpCallback != null) {
                        try {
                            Etag etag = Etag.etag(finalFile, UfileConstants.MULTIPART_SIZE);
                            httpCallback.onResponse(new DownloadFileBean()
                                    .setContentType(profile.getContentType())
                                    .seteTag(etag == null ? null : etag.geteTag())
                                    .setFile(finalFile)
                                    .setContentLength(finalFile.length())
                                    .setMetadatas(metadatas));
                        } catch (IOException e) {
                            httpCallback.onError(null,
                                    new ApiError(ApiError.ErrorType.ERROR_NORMAL_ERROR, new UfileIOException("Calculate ETag error!", e)), null);
                        }
                    }
                } catch (UfileClientException e) {
                    if (httpCallback != null)
                        httpCallback.onError(null, new ApiError(ApiError.ErrorType.ERROR_PARAMS_ILLEGAL, e), null);
                } catch (InterruptedException e) {
                    if (httpCallback != null)
                        httpCallback.onError(null, new ApiError(ApiError.ErrorType.ERROR_NORMAL_ERROR, "Invoke part occur error!", e), null);
                } finally {
                    mFixedThreadPool.shutdown();
                }
            }
        });
    }

    private class DownloadCallable implements Callable<DownloadFileBean> {
        private Call call;
        private long index;

        public DownloadCallable(Call call, long index) {
            this.call = call;
            this.index = index;
        }

        @Override
        public DownloadFileBean call() throws Exception {
            try {
                Response response = call.execute();
                if (response == null)
                    throw new UfileHttpException("Response is null");

                if (response.code() / 100 != 2)
                    throw new UfileHttpException(parseErrorResponse(response).toString());

                DownloadFileBean result = parseHttpResponse(response);
                if (index == 0 || index == (partCount - 1)) {
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
                }
                return result;
            } catch (Throwable t) {
                throw new UfileClientException(t);
            }
        }
    }

    @Override
    public DownloadFileBean parseHttpResponse(Response response) throws UfileIOException, NumberFormatException {
        InputStream is = null;
        RandomAccessFile raf = null;
        DownloadFileBean result = new DownloadFileBean();
        try {
            result.setContentLength(response.body().contentLength());
            result.setContentType(response.header("Content-Type"));
            String range = response.header("Content-Range", "");
            range = range.replace("bytes", "");
            String[] rangeArr = range.split("-");
            long start = Long.parseLong(rangeArr[0].trim());
            rangeArr = rangeArr[1].trim().split("/");
            long total = Long.parseLong(rangeArr[1].trim());
            JLog.T(TAG, "[Content-Range]:" + range + " [start]:" + start + " [total]:" + total);

            is = response.body().byteStream();

            raf = new RandomAccessFile(finalFile, "rwd");
            raf.seek(start - rangeStart);

            byte[] buffer = new byte[(int) bufferSize];
            int len = 0;
            while ((len = is.read(buffer)) > 0) {
                raf.write(buffer, 0, len);

                if (onProgressListener == null)
                    continue;

                long written = bytesWritten.addAndGet(len);
                long cache = bytesWrittenCache.addAndGet(len);

                if (progressConfig.type == ProgressConfig.ProgressIntervalType.PROGRESS_INTERVAL_TIME)
                    continue;

                if (written < total && cache < progressConfig.interval)
                    continue;

                bytesWrittenCache.set(0);
                onProgressListener.onProgress(written, total);
            }
        } catch (IOException e) {
            throw new UfileIOException("Occur IOException while IO stream");
        } finally {
            FileUtil.close(raf, is, response.body());
        }
        return result;
    }
}
