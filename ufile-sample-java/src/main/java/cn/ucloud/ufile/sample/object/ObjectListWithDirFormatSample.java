package cn.ucloud.ufile.sample.object;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.ApiError;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.bean.*;
import cn.ucloud.ufile.bean.ObjectListWithDirFormatBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.UfileCallback;
import cn.ucloud.ufile.sample.Constants;
import cn.ucloud.ufile.util.JLog;
import okhttp3.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-11 14:32
 */
public class ObjectListWithDirFormatSample {
    private static final String TAG = "ObjectListWithDirFormatSample";
    private static ObjectConfig config = new ObjectConfig("cn-sh2", "ufileos.com");

    public static void main(String[] args) {
        String bucketName = "";

        execute_list_all(bucketName);
    }

    //拉取一页列表
    public static void execute(String bucketName) {
        try {
            ObjectListWithDirFormatBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                    .objectListWithDirFormat(bucketName)
                    /**
                     * 过滤前缀
                     */
//                .withPrefix("")
                    /**
                     * 分页标记
                     */
//                .withMarker("")   //如果要拉下一页，withMarker 里要把 response.getNextMarker()  填进去，就可以拉下一页；
                    //如果 response.getNextMarker() 为"" 表示列表已经拉完了
                    //参考：https://github.com/ufilesdk-dev/elasticsearch-repository-ufile/blob/dev/src/main/java/org/elasticsearch/repository/ufile/UfileBlobStore.java，函数 listBlobsByPrefix
                    /**
                     * 分页数据上限，Default = 20
                     */
//                .dataLimit(10)
                    /**
                     * 目录分隔符，Default = '/'，当前只支持是'/'
                     */
                    .withDelimiter("/")
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
            List<CommonPrefix> directories = new ArrayList<>();
            String prefix = "";
            do {
                ObjectListWithDirFormatBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                        .objectListWithDirFormat(bucketName)
                        .withMarker(nextMarker)
                        .dataLimit(100)
                        .withPrefix(prefix)
                        /**
                         * 目录分隔符，Default = '/'，当前只支持是'/'
                         */
                        .withDelimiter("/")
                        .execute();
                //遍历结果
                if (response == null)
                    break;

                for (ObjectContentBean content : response.getObjectContents()) {
                    JLog.D(TAG, String.format("keyname: %s", content.getKey()));
                }
                for (CommonPrefix commonPrefix : response.getCommonPrefixes()) {
                    JLog.D(TAG, String.format("directory: %s", commonPrefix.getPrefix()));
                }
                //获取下一页
                nextMarker = response.getNextMarker();
                if (response.getCommonPrefixes() != null)
                    directories.addAll(response.getCommonPrefixes());
                if (directories.isEmpty())
                    prefix = null;
                if ((nextMarker == null || nextMarker.isEmpty()) && !directories.isEmpty()) {
                    nextMarker = "";
                    prefix = directories.remove(0).getPrefix();
                }
            } while ((nextMarker != null && nextMarker.length() > 0) || (prefix != null && prefix.length() > 0));

        } catch (UfileClientException e) {
            e.printStackTrace();
        } catch (UfileServerException e) {
            e.printStackTrace();
        }
    }

    public static void executeAsync(String bucketName) {
        UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
                .objectListWithDirFormat(bucketName)
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
                /**
                 * 目录分隔符，Default = '/'，当前只支持是'/'
                 */
                .withDelimiter("/")
                .executeAsync(new UfileCallback<ObjectListWithDirFormatBean>() {

                    @Override
                    public void onResponse(ObjectListWithDirFormatBean response) {
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
