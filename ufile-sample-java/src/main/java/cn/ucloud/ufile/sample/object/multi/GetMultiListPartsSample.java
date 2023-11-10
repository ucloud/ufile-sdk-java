package cn.ucloud.ufile.sample.object.multi;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.multi.MultiUploadListPartsInfo;
import cn.ucloud.ufile.api.object.multi.MultiUploadListPartsInfoApi;
import cn.ucloud.ufile.auth.BucketAuthorization;
import cn.ucloud.ufile.auth.ObjectAuthorization;
import cn.ucloud.ufile.auth.UfileBucketLocalAuthorization;
import cn.ucloud.ufile.auth.UfileObjectLocalAuthorization;
import cn.ucloud.ufile.bean.DownloadFileBean;
import cn.ucloud.ufile.bean.ObjectProfile;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.HttpClient;
import cn.ucloud.ufile.http.OnProgressListener;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GetMultiListPartsSample {
    private static final String TAG = "GetMultiListPartsInfo";
    private static ObjectConfig config = new ObjectConfig("cn-bj", "ufileos.com");

    public static void main(String[] args) {
        String keyName = "";
        String bucketName = "";
        /*填入初始化分片上传时的MultiUploadInfo.getUploadId*/
        getMultiListPartsInfo("");
    }

    public static void getMultiListPartsInfo(String uploadid) {
        try {
            MultiUploadListPartsInfo response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .multiUploadListPartsInfo(uploadid, "ukvtest")
                    /**
                     * 规定在US3响应中的最大Part数目
                     */
                .maxParts(5)
                    /**
                     * 指定List的起始位置，只有Part Number数目大于该参数的Part会被列出
                     */
 //            .partNumberMarker()
                    .execute();
            JLog.D(TAG, String.format("[res]!!!!!!!!!!!!!!!!!! = %s", (response == null ? "null" : response.toString())));
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }


}

