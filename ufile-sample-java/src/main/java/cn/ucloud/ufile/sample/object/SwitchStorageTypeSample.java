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
import cn.ucloud.ufile.util.StorageType;
import okhttp3.Request;


/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class SwitchStorageTypeSample {
    private static final String TAG = "SwitchStorageTypeSample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        String bucketName = "";
        String keyName = "";
        StorageType storageType = StorageType.STANDARD;
        switchStorageType(bucketName, keyName, storageType);
    }

    public static void switchStorageType(String bucketName, String keyName, StorageType storageType) {
        try {
            BaseResponseBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .switchStorageType(bucketName, keyName)
                    .turnTypeTo(storageType)
                    .execute();
            JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }

    public static void switchStorageTypeAsync(String bucketName, String keyName, StorageType storageType) {
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .switchStorageType(bucketName, keyName)
                .turnTypeTo(storageType)
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
