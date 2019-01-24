package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.bean.base.BaseResponseBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.UfileCallback;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;
import okhttp3.Request;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class UploadHitSample {
    private static final String TAG = "UploadHitSample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        File file = new File("");
        InputStream is = new ByteArrayInputStream(new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07});
        String keyName = "";
        String bucketName = "";
        uploadHitFile(file, keyName, bucketName);
    }

    public static void uploadHitFile(File file, String nameAs, String toBucket) {
        try {
            BaseResponseBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .uploadHit(file)
                    .nameAs(nameAs)
                    .toBucket(toBucket)
                    .execute();
            JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }

    public static void uploadHitFileAsync(File file, String nameAs, String toBucket) {
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .uploadHit(file)
                .nameAs(nameAs)
                .toBucket(toBucket)
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

    public static void uploadHitStream(InputStream stream, String nameAs, String toBucket) {
        try {
            BaseResponseBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .uploadHit(stream)
                    .nameAs(nameAs)
                    .toBucket(toBucket)
                    .execute();
            JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }

    public static void uploadHitStreamAsync(InputStream stream, String nameAs, String toBucket) {
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .uploadHit(stream)
                .nameAs(nameAs)
                .toBucket(toBucket)
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
