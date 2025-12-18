package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.GenerateObjectPrivateUploadUrlApi;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;
import cn.ucloud.ufile.util.MimeTypeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 预签名 URL 上传（PUT）示例
 * 注意：
 * - 预签名 PUT 的 Content-Type 会参与签名；上传时请求头必须与生成 URL 时一致（建议使用 urlInfo.getHeaders()）。
 */
public class PresignedUploadSample {
    private static final String TAG = "PresignedUploadSample";
    private static ObjectConfig config = new ObjectConfig("cn-bj", "ufileos.com");

    public static void main(String[] args) throws Exception {
        String bucketName = "";
        String keyName = "";
        String filePath = "";
        int expiresSeconds = 3600; // 预签名 URL 有效期（秒）

        // 可选：指定 Content-Type（会参与签名）；
        String contentType = "";

        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                throw new IllegalArgumentException("file not exists or not a file: " + file.getAbsolutePath());
            }

            // 自动检测文件类型
            contentType = MimeTypeUtil.getMimeType(file.getName());


            // Step 1：生成预签名 PUT URL
            GenerateObjectPrivateUploadUrlApi api = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .getUploadUrlToPrivateBucket(keyName, bucketName, expiresSeconds);

            // 可选：指定 Content-Type（参与签名；上传时必须一致）
            api.withContentType(contentType);

            // 可选：STS 临时密钥
            // SecurityToken 会被追加到预签名 URL 的 query 中；上传时无需放到请求头
            api.withSecurityToken(Constants.SECURITY_TOKEN);

            GenerateObjectPrivateUploadUrlApi.UploadUrlInfo urlInfo = api.createUrlWithHeaders();

            JLog.D(TAG, "Presigned PUT URL: " + urlInfo.getUrl());
            JLog.D(TAG, "Required headers: " + urlInfo.getHeaders());

            // 前后端分离：把 urlInfo.getUrl() + urlInfo.getHeaders() 返回给前端
            // 前端示意：
            // fetch(url, { method: 'PUT', headers, body: file })

            // Step 2：使用预签名 URL 上传文件
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(urlInfo.getUrl()).openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setDoOutput(true);
                conn.setRequestMethod("PUT");
                conn.setFixedLengthStreamingMode(file.length());

                // 上传时带上生成 URL 时给出的 headers：
                // - Content-Type：参与签名，必须一致
                if (urlInfo.getHeaders() != null) {
                    for (String k : urlInfo.getHeaders().keySet()) {
                        conn.setRequestProperty(k, urlInfo.getHeaders().get(k));
                    }
                }

                // 可选：上传时指定存储类型（默认 STANDARD）
                // conn.setRequestProperty("X-Ufile-Storage-Class", "IA"); // STANDARD | IA | ARCHIVE

                try (FileInputStream in = new FileInputStream(file);
                     OutputStream out = conn.getOutputStream()) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        out.write(buf, 0, n);
                    }
                    out.flush();
                }

                int code = conn.getResponseCode();
                JLog.D(TAG, "Upload http=" + code + ", message=" + conn.getResponseMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
