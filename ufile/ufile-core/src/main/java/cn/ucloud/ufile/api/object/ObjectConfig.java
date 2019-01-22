package cn.ucloud.ufile.api.object;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import cn.ucloud.ufile.exception.UfileFileException;
import cn.ucloud.ufile.util.FileUtil;

import java.io.*;

/**
 * 对象存储API配置选项
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/6 17:38
 */
public class ObjectConfig {
    /**
     * 仓库地区 (eg: 'cn-bj')
     */
    @SerializedName("Region")
    private String region;

    /**
     * 代理后缀 (eg: 'ufileos.com')
     */
    @SerializedName("ProxySuffix")
    private String proxySuffix;

    /**
     * 自定义域名 (eg: 'api.ucloud.cn')：若配置了非空自定义域名，则使用自定义域名，不会使用 region + proxySuffix 拼接
     */
    @SerializedName("CustomHost")
    private String customHost;

    public ObjectConfig(String region, String proxySuffix) {
        this.region = region;
        this.proxySuffix = proxySuffix;
    }

    public ObjectConfig(String customHost) {
        this.customHost = customHost;
    }

    /**
     * 从文件导入配置
     * 配置文件内容必须是含有以下参数的json字符串：{"Region":"","ProxySuffix":""} | {"CustomDomain":""}
     *
     * @param profile 配置文件
     * @return SDK配置
     * @throws UfileFileException
     */
    public static ObjectConfig loadProfile(File profile) throws UfileFileException {
        if (profile == null)
            throw new UfileFileException("Profile file is null!");

        if (!profile.exists())
            throw new UfileFileException("Profile file is inexistent!");

        if (!profile.isFile())
            throw new UfileFileException("Profile is not a file!");

        if (!profile.canRead())
            throw new UfileFileException("Profile file is not readable!");

        try {
            String content = FileUtil.readSmallFileStringContent(profile);
            return new Gson().fromJson(content, ObjectConfig.class);
        } catch (IOException e) {
            throw new UfileFileException(e);
        }
    }

    public String getRegion() {
        return region;
    }

    public ObjectConfig setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getProxySuffix() {
        return proxySuffix;
    }

    public ObjectConfig setProxySuffix(String proxySuffix) {
        this.proxySuffix = proxySuffix;
        return this;
    }

    public String getCustomHost() {
        return customHost;
    }

    public void setCustomHost(String customHost) {
        this.customHost = customHost;
    }

    public String host() {
        if (customHost == null || customHost.length() == 0)
            return String.format("%s.%s", region, proxySuffix);

        if (customHost.startsWith("http"))
            return customHost;

        return String.format("http://%s", customHost);
    }

    /**
     * 复制Config
     *
     * @param src 需复制的源src
     * @return
     */
    public static ObjectConfig copy(ObjectConfig src) {
        if (src == null)
            return null;

        if (src.customHost == null || src.customHost.length() == 0)
            return new ObjectConfig(src.region, src.proxySuffix);

        return new ObjectConfig(src.customHost);
    }
}
