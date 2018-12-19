package cn.ucloud.ufile.sample;

import cn.ucloud.ufile.auth.UfileBucketLocalAuthorization;
import cn.ucloud.ufile.auth.UfileObjectLocalAuthorization;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 16:13
 */
public class Constants {
    public static final UfileBucketLocalAuthorization BUCKET_AUTHORIZER = new UfileBucketLocalAuthorization(
            "public key",
            "private key");

    public static final UfileObjectLocalAuthorization OBJECT_AUTHORIZER = new UfileObjectLocalAuthorization(
            "public key",
            "private key");
}
