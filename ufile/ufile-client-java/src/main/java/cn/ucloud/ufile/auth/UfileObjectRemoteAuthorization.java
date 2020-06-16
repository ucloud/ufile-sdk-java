package cn.ucloud.ufile.auth;

import cn.ucloud.ufile.http.request.PostJsonRequestBuilder;
import cn.ucloud.ufile.util.FileUtil;
import cn.ucloud.ufile.util.JLog;
import com.google.gson.JsonObject;
import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

/**
 * Ufile默认的远程签名生成器
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/7 15:31
 */
public final class UfileObjectRemoteAuthorization extends ObjectRemoteAuthorization {
    private final String TAG = getClass().getSimpleName();

    /**
     * 构造方法
     *
     * @param publicKey 用户公钥
     * @param apiConfig 远程授权接口配置
     */
    public UfileObjectRemoteAuthorization(String publicKey, ApiConfig apiConfig) {
        super(publicKey, apiConfig);
    }

    @Override
    public String authorization(ObjectOptAuthParam param) {
        JsonObject json = new JsonObject();
        json.addProperty("method", param.getMethod().getName());
        json.addProperty("bucket", param.getBucket());
        json.addProperty("key", param.getKeyName());
        json.addProperty("content_type", param.getContentType());
        json.addProperty("content_md5", param.getContentMD5());
        json.addProperty("date", param.getDate());
        if (param.getOptional() != null)
            json.addProperty("optional", param.getOptional().toString());
        if (param.getPutPolicy() != null && param.getPutPolicy().getPolicy() != null)
            json.addProperty("put_policy", param.getPutPolicy().getPolicy());

        Call call = new PostJsonRequestBuilder()
                .baseUrl(apiConfig.getObjectOptAuthServer())
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .params(json)
                .build(httpClient.getOkHttpClient());
        Response response = null;
        try {
            response = call.execute();
            String signautre = response.body().string();
            JLog.D(TAG, signautre);
            return signautre;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            FileUtil.close(response.body());
        }
    }

    @Override
    public String authorizePrivateUrl(ObjectDownloadAuthParam param) {
        JsonObject json = new JsonObject();
        json.addProperty("method", param.getMethod().getName());
        json.addProperty("bucket", param.getBucket());
        json.addProperty("key", param.getKeyName());
        json.addProperty("expires", param.getExpires());
        if (param.getOptional() != null)
            json.addProperty("optional", param.getOptional().toString());

        Call call = new PostJsonRequestBuilder()
                .baseUrl(apiConfig.getObjectDownloadAuthServer())
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .params(json)
                .build(httpClient.getOkHttpClient());

        Response response = null;
        try {
            response = call.execute();
            String signautre = response.body().string();
            JLog.D(TAG, signautre);
            return signautre;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            FileUtil.close(response.body());
        }
    }
}
