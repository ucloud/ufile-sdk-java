package cn.ucloud.ufile.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/28 15:26
 */
public class BucketInfoBean implements Serializable {
    @SerializedName("Biz")
    private String biz;
    @SerializedName("BucketId")
    private String bucketId;
    @SerializedName("BucketName")
    private String bucketName;
    @SerializedName("CreateTime")
    private long createTime;
    @SerializedName("Domain")
    private DomainBean domain;
    @SerializedName("HasUserDomain")
    private int hasUserDomain;
    @SerializedName("ModifyTime")
    private long modifyTime;
    @SerializedName("Region")
    private String region;
    @SerializedName("Type")
    private String type;
    @SerializedName("CdnDomainId")
    private List<String> cdnDomainIds;

    public String getBiz() {
        return biz;
    }

    public void setBiz(String biz) {
        this.biz = biz;
    }

    public String getBucketId() {
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public DomainBean getDomain() {
        return domain;
    }

    public void setDomain(DomainBean domain) {
        this.domain = domain;
    }

    public int getHasUserDomain() {
        return hasUserDomain;
    }

    public void setHasUserDomain(int hasUserDomain) {
        this.hasUserDomain = hasUserDomain;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getCdnDomainIds() {
        return cdnDomainIds;
    }

    public void setCdnDomainIds(List<String> cdnDomainIds) {
        this.cdnDomainIds = cdnDomainIds;
    }

    public static class DomainBean {
        @SerializedName("Cdn")
        private List<String> cdn;
        @SerializedName("CustomCdn")
        private List<String> customCdn;
        @SerializedName("CustomSrc")
        private List<String> customSrc;
        @SerializedName("Src")
        private List<String> src;

        public List<String> getCdn() {
            return cdn;
        }

        public void setCdn(List<String> cdn) {
            this.cdn = cdn;
        }

        public List<String> getCustomCdn() {
            return customCdn;
        }

        public void setCustomCdn(List<String> customCdn) {
            this.customCdn = customCdn;
        }

        public List<String> getCustomSrc() {
            return customSrc;
        }

        public void setCustomSrc(List<String> customSrc) {
            this.customSrc = customSrc;
        }

        public List<String> getSrc() {
            return src;
        }

        public void setSrc(List<String> src) {
            this.src = src;
        }

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }
    
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
