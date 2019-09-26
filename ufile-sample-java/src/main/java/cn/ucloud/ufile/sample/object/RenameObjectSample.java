package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.bean.base.BaseResponseBean;
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
public class RenameObjectSample {
    private static final String TAG = "RenameObjectSample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        String bucketName = "";
        String keyName = "";
        String newKeyName = "";
        boolean isForceToCover = false;
        renameObject(bucketName, keyName, newKeyName, isForceToCover);
    }

    public static void renameObject(String bucketName, String keyName, String newKeyName, boolean isForceToCover) {
        try {
            BaseResponseBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .renameObject(bucketName, keyName)
                    .isRenamedTo(newKeyName)
                    /**
                     * 如果已存在同名文件，值为true则覆盖，否则操作失败；请求中若不携带该参数，默认不覆盖
                     */
//                    .isForcedToCover(isForceToCover)
                    .execute();
            JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }

    public static void renameObjectAsync(String bucketName, String keyName, String newKeyName, boolean isForceToCover) {
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .renameObject(bucketName, keyName)
                .isRenamedTo(newKeyName)
                /**
                 * 如果已存在同名文件，值为true则覆盖，否则操作失败；请求中若不携带该参数，默认不覆盖
                 */
//                    .isForcedToCover(isForceToCover)
                .executeAsync(new UfileCallback<BaseResponseBean>() {

                    @Override
                    public void onResponse(BaseResponseBean response) {
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
