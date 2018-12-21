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
            "TOKEN_7a484b2f-c4ca-471c-a9bc-8663f39580b9",
            "9c26bd2b-4c68-41a1-ad9a-a633d47bb821");
}
