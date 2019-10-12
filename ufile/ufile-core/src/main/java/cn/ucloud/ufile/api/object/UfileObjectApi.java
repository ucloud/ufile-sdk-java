package cn.ucloud.ufile.api.object;

import cn.ucloud.ufile.auth.ObjectAuthorizer;
import cn.ucloud.ufile.api.UfileApi;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.http.HttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Ufile 对象存储相关API基类
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/13 11:22
 */
public abstract class UfileObjectApi<T> extends UfileApi<T> {
    protected ObjectAuthorizer authorizer;

    /**
     * 构造方法
     *
     * @param authorizer Object授权器
     * @param host       API域名
     * @param httpClient Http客户端
     */
    public UfileObjectApi(ObjectAuthorizer authorizer, String host, HttpClient httpClient) {
        super(httpClient, host);
        this.authorizer = authorizer;
    }

    /**
     * 生成最终API域名
     *
     * @param bucketName bucket名称
     * @param keyName    对象名称
     * @return API域名
     */
    protected String generateFinalHost(String bucketName, String keyName) throws UfileClientException {
        if (host == null || host.length() == 0)
            return host;

        if (host.startsWith("http"))
            return String.format("%s/%s", host, keyName);

        try {
            bucketName = URLEncoder.encode(bucketName, "UTF-8").replace("+", "%20");
            keyName = URLEncoder.encode(keyName, "UTF-8").replace("+", "%20");
            return String.format("http://%s.%s/%s", bucketName, host, keyName);
        } catch (UnsupportedEncodingException e) {
            throw new UfileClientException("Occur error during URLEncode bucketName and keyName", e);
        }
    }

    protected String readResponseBody(Response response) {
        if (response == null)
            return null;
        ResponseBody body = response.body();
        if (body == null)
            return null;

        try {
            return body.string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
