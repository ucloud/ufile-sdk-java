package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.bean.CopyObjectResultBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.UfileCallback;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;
import okhttp3.Request;


/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class CopyObjectSample {
    private static final String TAG = "CopyObjectSample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        String srcBucket = "";
        String srcKeyName = "";
        String dstBucket = "";
        String dstKeyName = "";
        copyObjectAsync(srcBucket, srcKeyName, dstBucket, dstKeyName);
    }

    public static void copyObject(String srcBucket, String srcKeyName, String dstBucket, String dstKeyName) {
        try {
            CopyObjectResultBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .copyObject(srcBucket, srcKeyName)
                    .copyTo(dstBucket, dstKeyName)
                    .execute();
            JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }

    public static void copyObjectAsync(String srcBucket, String srcKeyName, String dstBucket, String dstKeyName) {
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .copyObject(srcBucket, srcKeyName)
                .copyTo(dstBucket, dstKeyName)
                .executeAsync(new UfileCallback<CopyObjectResultBean>() {

                    @Override
                    public void onResponse(CopyObjectResultBean response) {
                        JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
                    }

                    @Override
                    public void onError(Request request, ApiError error, UfileErrorBean response) {
                        JLog.D(TAG, String.format("[error] = %s\n[info] = %s",
                                (error == null ? "null" : error.toString()),
                                (response == null ? "null" : response.toString())));
                    }
                });
    }

}
