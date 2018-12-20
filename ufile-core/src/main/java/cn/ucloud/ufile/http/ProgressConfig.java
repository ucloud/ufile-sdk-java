package cn.ucloud.ufile.http;

/**
 * 进度回调设置
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-04 18:32
 */
public class ProgressConfig {
    /**
     * 默认进度回调时间周期：1000 ms
     */
    public static final int DEFAULT_PROGRESS_PERIOD = 1000;
    /**
     * 默认进度回调buffer size：1 MB
     */
    public static final long DEFAULT_PROGRESS_BUFFER_SIZE = 1 << 20;
    /**
     * 默认进度回调百分比：10 %
     */
    public static final int DEFAULT_PROGRESS_PERCENT = 10;

    public enum ProgressIntervalType {
        PROGRESS_INTERVAL_TIME,
        PROGRESS_INTERVAL_PERCENT,
        PROGRESS_INTERVAL_BUFFER
    }

    public ProgressIntervalType type;
    public long interval;

    private ProgressConfig(ProgressIntervalType type) {
        this.type = type;
    }

    /**
     * 默认回调配置 ( -> callbackWithPeriod(1000) )
     *
     * @return 进度回调设置
     */
    public static ProgressConfig callbackDefault() {
        ProgressConfig config = new ProgressConfig(ProgressIntervalType.PROGRESS_INTERVAL_TIME);
        config.interval = 1000;
        return config;
    }

    /**
     * 按时间周期回调
     *
     * @param period 回调间隔时间 (ms)，如果period = 0，则实时回调
     * @return 进度回调设置
     */
    public static ProgressConfig callbackWithPeriod(int period) {
        ProgressConfig config = new ProgressConfig(ProgressIntervalType.PROGRESS_INTERVAL_TIME);
        config.interval = period;
        return config;
    }

    /**
     * 按总大小百分比回调
     *
     * @param percent 百分比 (0 ~ 100)，如果percent = 0，则实时回调
     * @return 进度回调设置
     */
    public static ProgressConfig callbackWithPercent(int percent) {
        ProgressConfig config = new ProgressConfig(ProgressIntervalType.PROGRESS_INTERVAL_PERCENT);
        config.interval = percent;
        return config;
    }

    /**
     * 按每传输多少内容回调，如果size = 0，则实时回调
     *
     * @param size 内容片大小 (byte size)
     * @return 进度回调设置
     */
    public static ProgressConfig callbackWithBuffer(long size) {
        ProgressConfig config = new ProgressConfig(ProgressIntervalType.PROGRESS_INTERVAL_BUFFER);
        config.interval = size;
        return config;
    }
}
