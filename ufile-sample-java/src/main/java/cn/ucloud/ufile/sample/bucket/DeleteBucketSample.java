package cn.ucloud.ufile.sample.bucket;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.bean.BucketResponse;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileException;
import cn.ucloud.ufile.http.UfileCallback;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;
import okhttp3.Request;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class DeleteBucketSample {
    private static final String TAG = "DeleteBucketSample";

    public static void main(String[] args) {
        String bucketName = "";

        execute(bucketName);
    }

    public static void execute(String bucketName) {
        try {
            BucketResponse res = UfileClient.bucket(Constants.BUCKET_AUTHORIZER)
                    .deleteBucket(bucketName)
                    .execute();
            JLog.D(TAG, String.format("[res] = %s", (res == null ? "null" : res.toString())));
        } catch (UfileException e) {
            e.printStackTrace();
        }
    }

    public static void executeAsync(String bucketName) {
        UfileClient.bucket(Constants.BUCKET_AUTHORIZER)
                .deleteBucket(bucketName)
                .executeAsync(new UfileCallback<BucketResponse>() {
                    @Override
                    public void onResponse(BucketResponse response) {
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
