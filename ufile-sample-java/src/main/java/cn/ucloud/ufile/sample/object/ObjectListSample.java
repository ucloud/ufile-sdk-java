package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.bean.ObjectInfoBean;
import cn.ucloud.ufile.bean.ObjectListBean;
import cn.ucloud.ufile.bean.UfileErrorBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.UfileCallback;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;
import okhttp3.Request;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class ObjectListSample {
    private static final String TAG = "ObjectListSample";
    private static ObjectConfig config = new ObjectConfig("cn-bj", "ufileos.com");

    public static void main(String[] args) {
        String bucketName = "";

        execute_list_all(bucketName);
    }

    //拉取一页列表
    public static void execute(String bucketName) {
        try {
            ObjectListBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .objectList(bucketName)
                    /**
                     * 过滤前缀
                     */
//                .withPrefix("")
                    /**
                     * 分页标记
                     */
//                .withMarker("")
                    /**
                     * 分页数据上限，Default = 20
                     */
//                .dataLimit(10)
                    .execute();
            JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }

    //拉取全部文件列表
    /*关于分页拉列表：
    第一次拉： nextMarker 填 "",  返回值里，会有 nextMarker 的值  "xxxx"， 返回 0-99 的结果
    第二次拉： nextMarker 填 "xxxx",  就可以拉  100-199  的结果，通过会返回新的 nextMarker "yyyy"
    第三次拉： nextMarker 填 "yyyy",  就可以拉  200-299  的结果，通过会返回新的 nextMarker "zzzz"
            。。。。
    直到， 如果拉到列表尾部了， nextMarker 会返回长度为0的串,  表示到达尾部； 就拉完了， 不需要继续拉列表
    https://docs.ucloud.cn/api/ufile-api/prefix_file_list
    */
    public static void execute_list_all(String bucketName) {
        try {
            String nextMarker = "";
            do {
                ObjectListBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                        .objectList(bucketName)
                        .withMarker(nextMarker)
                        .dataLimit(100)
                        .execute();
                //遍历结果
                for (ObjectInfoBean objInfo : response.getObjectList()) {
                    JLog.D(TAG, String.format("keyname: %s", objInfo.getFileName()));
                }
                //获取下一页
                nextMarker = response.getNextMarker();
                JLog.D(TAG, String.format("[res] = %s", (response == null ? "null" : response.toString())));
            }while(nextMarker != null && nextMarker.length() != 0);

        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }

    public static void executeAsync(String bucketName) {
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .objectList(bucketName)
                /**
                 * 过滤前缀
                 */
//                .withPrefix("")
                /**
                 * 分页标记
                 */
//                .withMarker("")
                /**
                 * 分页数据上限，Default = 20
                 */
//                .dataLimit(10)
                .executeAsync(new UfileCallback<ObjectListBean>() {

                    @Override
                    public void onResponse(ObjectListBean response) {
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
