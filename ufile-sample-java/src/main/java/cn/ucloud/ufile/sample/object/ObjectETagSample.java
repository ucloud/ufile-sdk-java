package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.bean.ObjectProfile;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.Etag;
import cn.ucloud.ufile.util.JLog;

import java.io.*;

/**
 * @author: clark.liu
 * @E-mail: clark.liu@ucloud.cn
 * @date: 2021-02-23
 */
public class ObjectETagSample {
    private static final String TAG = "GetObjectETagSample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        computeLocalFileETag();
        // computeLocalFileETagUseStream();
        // getBucketObjectETag();
        // compareETag();
    }

    public static void computeLocalFileETag() {
        String path = "";
        try {
            File file = new File(path);
            Etag etag = Etag.etag(file);
            JLog.D(TAG, String.format("ETag is [%s]: file path [%s]", etag.geteTag(), path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void computeLocalFileETagUseStream() {
        String path = "";
        try {
            InputStream in = new FileInputStream(path);
            Etag etag = Etag.etag(in);
            JLog.D(TAG, String.format("ETag is [%s]: file path [%s]", etag.geteTag(), path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getBucketObjectETag() {
        String keyName = "";
        String bucketName = "";

        try {
            ObjectProfile objectProfile = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .objectProfile(keyName, bucketName).execute();

            if (objectProfile == null) {
                JLog.D(TAG, String.format("key [%s] may be not exist", keyName));
                return;
            }

            JLog.D(TAG, String.format("key [%s] ETag: %s", keyName, objectProfile.geteTag()));
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }

    public static void compareETag() {
        String keyName = "";
        String bucketName = "";
        String localpath = "";

        try {
            File f = new File(localpath);
            if (UfileClient.object(Constants.OBJECT_AUTHORIZER, config).compareEtag(f, keyName, bucketName)) {
                JLog.D(TAG, "ETag compare success");
            } else {
                JLog.D(TAG, "ETag compare fail");
            }
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }
}
