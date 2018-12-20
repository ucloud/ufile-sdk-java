package cn.ucloud.ufile.http.request.body;

import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.util.FileUtil;
import okhttp3.MediaType;
import okio.BufferedSink;
import okio.Okio;

import java.io.IOException;
import java.io.InputStream;

/**
 * 上传流请求体，用于拦截上传进度进行回调
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/9 15:04
 */
public class UploadStreamRequestBody extends UploadRequestBody<InputStream> {

    /**
     * 构造方法
     *
     * @param inputStream    流
     * @param contentType    数据流MIME类型(Content-Type)
     * @param contentLength  数据流长度
     * @param uploadListener 进度回调监听
     */
    public UploadStreamRequestBody(InputStream inputStream, MediaType contentType, long contentLength,
                                   OnProgressListener uploadListener) {
        super(inputStream, contentType, uploadListener);
        this.contentLength = contentLength;
    }

    @Override
    public UploadRequestBody setContent(InputStream content) {
        this.content = content;
        try {
            this.contentLength = content.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        try {
            doWriteTo(sink, Okio.source(content));
        } finally {
            FileUtil.close(content);
        }
    }

}
