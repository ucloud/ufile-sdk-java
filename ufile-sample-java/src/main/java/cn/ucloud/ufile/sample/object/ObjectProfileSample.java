package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.bean.ObjectProfile;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.UfileCallback;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;
import okhttp3.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class ObjectProfileSample {
    private static final String TAG = "ObjectProfileSample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        String keyName = "";
        String bucketName = "";

        execute(keyName, bucketName);
        List<Info> infos = new ArrayList<>();
        infos.add(new Info(bucketName).setKeyName(""));
        infos.add(new Info(bucketName).setKeyName(""));
        infos.add(new Info(bucketName).setKeyName(""));
        infos.add(new Info(bucketName).setKeyName(""));
        infos.add(new Info(bucketName).setKeyName(""));
//        batch(infos);
    }

    private static class Info {
        private String bucket;
        private String keyName;

        public Info(String bucket) {
            this.bucket = bucket;
        }

        public String getBucket() {
            return bucket;
        }

        public Info setBucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public String getKeyName() {
            return keyName;
        }

        public Info setKeyName(String keyName) {
            this.keyName = keyName;
            return this;
        }
    }

    public static void batch(List<Info> infos) {
        if (infos == null || infos.isEmpty())
            return;

        ExecutorService threadPool = new ThreadPoolExecutor(5, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        List<ObjectProfileCallable> callables = new ArrayList<>();
        for (Info info : infos) {
            callables.add(new ObjectProfileCallable(info));
        }

        try {
            List<Future<ObjectProfile>> futures = threadPool.invokeAll(callables);
            for (Future<ObjectProfile> future : futures) {
                try {
                    JLog.D(TAG, "=====================================================================\n");
                    ObjectProfile objectProfile = future.get();
                    JLog.D(TAG, String.format("[res]: %s", (objectProfile == null ? "null" : objectProfile.toString())));
                    Map<String, String> headers = objectProfile.getHeaders();
                    if (headers != null) {
                        for (Map.Entry<String, String> entry : headers.entrySet()) {
                            JLog.D(TAG, "\t\t[key]:" + entry.getKey() + "\t[val]:" + entry.getValue());
                        }
                    }
                } catch (ExecutionException e) {
                    JLog.D(TAG, String.format("[err]: %s", e.getMessage()));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdownNow();
        }
    }

    private static class ObjectProfileCallable implements Callable<ObjectProfile> {
        private Info info;

        public ObjectProfileCallable(Info info) {
            this.info = info;
        }

        @Override
        public ObjectProfile call() throws Exception {
            Thread.sleep(5000);
            return UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .objectProfile(info.getKeyName(), info.getBucket())
                    .execute();
        }
    }

    public static void execute(String keyName, String bucketName) {
        String securityToken = Constants.SECURITY_TOKEN;
        try {
            ObjectProfile objectProfile = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .objectProfile(keyName, bucketName)
                    .withSecurityToken(securityToken)
                    .execute();
            JLog.D(TAG, String.format("[res]: %s", (objectProfile == null ? "null" : objectProfile.toString())));
            Map<String, String> headers = objectProfile.getHeaders();
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    JLog.D(TAG, "[key]:" + entry.getKey() + " [val]:" + entry.getValue());
                }
            }
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }

    public static void executeAsync(String keyName, String bucketName) {
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .objectProfile(keyName, bucketName)
                .executeAsync(new UfileCallback<ObjectProfile>() {

                    @Override
                    public void onResponse(ObjectProfile response) {
                        JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
                        Map<String, String> headers = response.getHeaders();
                        if (headers != null) {
                            for (Map.Entry<String, String> entry : headers.entrySet()) {
                                JLog.D(TAG, "[key]:" + entry.getKey() + " [val]:" + entry.getValue());
                            }
                        }
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
