package cn.ucloud.ufile.http;

/**
 * 进度监听器
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/16 11:40
 */
public interface OnProgressListener {
    void onProgress(long bytesWritten, long contentLength);
}
