package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.ObjectRestoreApi;
import cn.ucloud.ufile.bean.ObjectRestoreBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.http.UfileCallback;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;
import okhttp3.Request;

import static cn.ucloud.ufile.UfileClient.object;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class ObjectRestoreSample {
    private static final String TAG = "ObjectRestoreSample";
    private static ObjectConfig config = new ObjectConfig("cn-gd", "ufileos.com");

    public static void main(String[] args) {
        String keyName = "a.jpg";
        String bucketName = "ufile-test-gd";
        try {
           ObjectRestoreBean a = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .objectRestore(keyName, bucketName)
                    .execute();    //同步调用，如果要用异步调用，请用 executeAsync(...)

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
