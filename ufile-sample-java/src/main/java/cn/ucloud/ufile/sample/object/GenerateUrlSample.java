package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.sample.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 生成对象URL的示例
 */
public class GenerateUrlSample {
    private static final String TAG = "GenerateUrlSample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        String keyName = "";        // 对象名称
        String bucketName = "";     // 存储空间名称
        
        // 生成公有空间的URL
        try {
            String publicUrl = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .getDownloadUrlFromPublicBucket(keyName, bucketName)
                    // 如果是图片，可以添加处理参数
//                    .withIopCmd("iopcmd=thumbnail&type=1&scale=50")
                    .createUrl();
            System.out.println("公有空间URL: " + publicUrl);
            
        } catch (UfileClientException e) {
            e.printStackTrace();
        }

        // 生成私有空间的URL（5分钟有效期）
        try {
            String securityToken = Constants.SECURITY_TOKEN;
            String privateUrl = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .getDownloadUrlFromPrivateBucket(keyName, bucketName, 5 * 60)
                    /**
                     * 使用安全令牌
                     */
                    .withSecurityToken(securityToken)
                    // 如果是图片，可以添加处理参数
//                    .withIopCmd("iopcmd=thumbnail&type=1&scale=50")
                    .createUrl();
            System.out.println("私有空间URL（5分钟有效）: " + privateUrl);
            
        } catch (UfileClientException e) {
            e.printStackTrace();
        }

        // 生成强制下载的URL（不在浏览器中预览）
        try {
            String downloadUrl = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .getDownloadUrlFromPublicBucket(keyName, bucketName)
                    .withAttachment()  // 使用默认文件名（同对象名）
                    // 或者指定下载时的文件名
                    // .withAttachment("custom-filename.jpg")
                    .createUrl();
            System.out.println("强制下载URL: " + downloadUrl);

        } catch (UfileClientException e) {
            e.printStackTrace();
        }




    }
} 