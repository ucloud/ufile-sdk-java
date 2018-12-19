package cn.ucloud.ufile.sample.bucket;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.bean.BucketDescribeResponse;
import cn.ucloud.ufile.exception.UfileException;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class DescribeBucketSample {
    private static final String TAG = "DescribeBucketSample";

    public static void main(String[] args) {
        try {
            BucketDescribeResponse res = UfileClient.bucket(Constants.BUCKET_AUTHORIZER)
                    .describeBucket()
                    /**
                     * 指定bucketName查询
                     */
//                    .whichBucket(bucketName)
                    /**
                     * 指定查询分页范围
                     */
//                    .withOffsetAndLimit(0,10)
                    /**
                     * 指定projectId
                     */
//                    .withProjectId(projectId)
                    .execute();
            JLog.D(TAG, String.format("[res] = %s", (res == null ? "null" : res.toString())));
        } catch (UfileException e) {
            e.printStackTrace();
        }
    }
}
