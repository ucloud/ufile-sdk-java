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
import cn.ucloud.ufile.util.MetadataDirective;
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
                    /**
                     * 为云端对象配置自定义数据，每次调用将会替换之前数据。
                     * 所有的自定义数据总大小不能超过 8KB。
                     */
//                    .withMetaDatas()
                    /**
                     * 为云端对象添加自定义数据，可直接调用，无须先调用withMetaDatas
                     * key不能为空或者""
                     *
                     */
//                    .addMetaData(new Parameter<>("key","value"))
                    /**
                     * 配置用户自定义元数据设置方式
                     * 具体参数配置可见 {@link MetadataDirective}
                     */
//                    .withMetadataDirective(MetadataDirective.COPY)
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
                /**
                 * 为云端对象配置自定义数据，每次调用将会替换之前数据。
                 * 所有的自定义数据总大小不能超过 8KB。
                 */
//                    .withMetaDatas()
                /**
                 * 为云端对象添加自定义数据，可直接调用，无须先调用withMetaDatas
                 * key不能为空或者""
                 *
                 */
//                    .addMetaData(new Parameter<>("key","value"))
                /**
                 * 配置用户自定义元数据设置方式
                 * 具体参数配置可见 {@link MetadataDirective}
                 */
//                .withMetadataDirective(MetadataDirective.COPY)
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
