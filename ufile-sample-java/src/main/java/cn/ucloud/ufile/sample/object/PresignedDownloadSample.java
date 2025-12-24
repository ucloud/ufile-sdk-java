package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.GenerateObjectPrivateUrlApi;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 预签名 URL 下载（GET）示例

 */
public class PresignedDownloadSample {
    private static final String TAG = "PresignedDownloadSample";
    private static ObjectConfig config = new ObjectConfig("cn-bj", "ufileos.com");

    public static void main(String[] args) throws Exception {
        String bucketName = "";
        String keyName = "";
        String savePath = "";
        int expiresSeconds = 600;

        try {
            // Step 1：生成预签名 GET URL
            GenerateObjectPrivateUrlApi api = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .getDownloadUrlFromPrivateBucket(keyName, bucketName, expiresSeconds);

            // 可选：STS 临时密钥
            api.withSecurityToken(Constants.SECURITY_TOKEN);

            String url = api.createUrl();

            JLog.D(TAG, "Presigned GET URL: " + url);

            // Step 2：使用预签名 URL 下载
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("GET");

            int code = conn.getResponseCode();
            if (code != 200) {
                throw new RuntimeException("download failed, http=" + code + ", message=" + conn.getResponseMessage());
            }

            File saveAs = new File(savePath);
            File parent = saveAs.getParentFile();
            if (parent != null && !parent.exists()) {
                //noinspection ResultOfMethodCallIgnored
                parent.mkdirs();
            }

            try (InputStream in = conn.getInputStream();
                 OutputStream out = new FileOutputStream(saveAs)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                }
                out.flush();
            }

            JLog.D(TAG, "Download success: " + saveAs.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
