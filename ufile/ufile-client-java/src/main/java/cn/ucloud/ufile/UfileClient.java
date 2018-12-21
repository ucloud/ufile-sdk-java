package cn.ucloud.ufile;

import cn.ucloud.ufile.auth.BucketAuthorizer;
import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.api.bucket.BucketApiBuilder;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.api.object.ObjectApiBuilder;
import cn.ucloud.ufile.http.HttpClient;

/**
 * Ufile SDK
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/9 17:08
 */
public class UfileClient {
    private static volatile UfileClient mInstance;
    private HttpClient httpClient;

    private UfileClient() {
        this.httpClient = new HttpClient();
    }

    private static UfileClient createClient() {
        if (mInstance == null) {
            synchronized (UfileClient.class) {
                if (mInstance == null)
                    mInstance = new UfileClient();
            }
        }

        return mInstance;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public static BucketApiBuilder bucket(BucketAuthorizer authorizer) {
        return new BucketApiBuilder(createClient(), authorizer);
    }

    public static ObjectApiBuilder object(ObjectAuthorizer authorizer, ObjectConfig config) {
        return new ObjectApiBuilder(createClient(), authorizer, config.host());
    }
}
