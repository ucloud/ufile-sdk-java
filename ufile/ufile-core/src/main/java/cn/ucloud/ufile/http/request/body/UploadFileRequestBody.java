package cn.ucloud.ufile.http.request.body;

import cn.ucloud.ufile.http.OnProgressListener;
import okhttp3.MediaType;
import okio.*;

import java.io.File;
import java.io.IOException;

/**
 * 上传文件请求体，用于拦截上传进度进行回调
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/9 15:04
 */
public class UploadFileRequestBody extends UploadRequestBody<File> {

    /**
     * 构造方法
     *
     * @param content        文件
     * @param contentType    文件MIME类型(Content-Type)
     * @param uploadListener 进度回调监听
     */
    public UploadFileRequestBody(File content, MediaType contentType, OnProgressListener uploadListener) {
        super(content, contentType, uploadListener);
        this.contentLength = content.length();
    }

    @Override
    public UploadRequestBody setContent(File content) {
        this.content = content;
        this.contentLength = content.length();
        return this;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        doWriteTo(sink, Okio.source(content));
    }
}
