package cn.ucloud.ufile.util;

/**
 * @description:
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2019/11/25 14:49
 */
public class MetadataDirective {
    /**
     * CopyObjectApi：复制源File的元数据到目标File。
     * 说明：如果拷贝操作的源File地址和目标File地址相同，则无论X-Ufile-Metadata-Directive为何值，都会直接替换源File的元数据。
     */
    public static final String COPY = "COPY";
    /**
     * CopyObjectApi：忽略源File的元数据，直接采用请求中指定的元数据。
     * 说明：如果拷贝操作的源File地址和目标File地址相同，则无论X-Ufile-Metadata-Directive为何值，都会直接替换源File的元数据。
     * <p>
     * FinishMultipartUploadApi：忽略初始化分片时设置的用户自定义元数据，直接采用Finish请求中指定的元数据。
     */
    public static final String REPLACE = "REPLACE";
    /**
     * FinishMultipartUploadApi：保持初始化时设置的用户自定义元数据不变。
     */
    public static final String UNCHANGED = "UNCHANGED";
}
