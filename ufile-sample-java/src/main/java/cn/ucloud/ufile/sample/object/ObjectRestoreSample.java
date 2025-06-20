package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.bean.base.BaseObjectResponseBean;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class ObjectRestoreSample {
    private static final String TAG = "ObjectRestoreSample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");


    public static void main(String[] args) {
        String keyName = "";
        String bucketName = "";
        String securityToken = Constants.SECURITY_TOKEN;
        try {
            BaseObjectResponseBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .objectRestore(keyName, bucketName)
                    .withSecurityToken(securityToken)
                    .execute();    //同步调用，如果要用异步调用，请用 executeAsync(...)
            JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
