package cn.ucloud.ufile.api.object.policy;

import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.util.Base64;
import cn.ucloud.ufile.util.JLog;
import com.google.gson.JsonObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2019/9/23 14:35
 */
public final class PutPolicyForCallback extends PutPolicy {
    private String policyContent;

    private PutPolicyForCallback(String policyContent, String policy) throws UfileClientException {
        super(policy);
        this.policyContent = policyContent;
        JLog.D(TAG, "[PutPolicyContent]:" + policyContent);
    }

    public String getPolicyContent() {
        return policyContent;
    }

    public static class Builder extends PutPolicy.Builder<PutPolicyForCallback> {
        private String callbackUrl;
        private String callbackBodyType;
        private List<PolicyParam> callbackBody;

        public Builder(String callbackUrl) {
            this(callbackUrl, new ArrayList<PolicyParam>());
        }

        public Builder(String callbackUrl, List<PolicyParam> callbackBody) {
            this(callbackUrl, callbackBody, null);
        }

        public Builder(String callbackUrl, List<PolicyParam> callbackBody, String callbackBodyType) {
            this.callbackUrl = callbackUrl;
            this.callbackBody = callbackBody;
            this.callbackBodyType = callbackBodyType;
        }

        public Builder setCallbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
            return this;
        }

        public Builder setCallbackBody(List<PolicyParam> callbackBody) {
            this.callbackBody = callbackBody;
            return this;
        }

        public Builder setCallbackBodyType(String callbackBodyType) {
            this.callbackBodyType = callbackBodyType;
            return this;
        }

        public Builder addCallbackBody(PolicyParam param) {
            if (param == null)
                return this;

            if (this.callbackBody == null)
                this.callbackBody = new ArrayList<>();

            this.callbackBody.add(param);
            return this;
        }

        public String getCallbackUrl() {
            return callbackUrl;
        }

        public List<PolicyParam> getCallbackBody() {
            return callbackBody;
        }

        public String getCallbackBodyType() {
            return callbackBodyType;
        }

        public PutPolicyForCallback build() throws UfileClientException {
            if (callbackUrl == null || callbackUrl.isEmpty())
                throw new UfileClientException("callbackUrl can not be null or empty in PutPolicyForCallback");

            JsonObject json = new JsonObject();
            json.addProperty("callbackUrl", callbackUrl);
            if (callbackBodyType != null && !callbackBodyType.isEmpty())
                json.addProperty("callbackBodyType", callbackBodyType);

            StringBuilder sb = new StringBuilder();
            if (callbackBody != null) {
                if (callbackBodyType != null && callbackBodyType.startsWith("application/json")) {
                    JsonObject body = new JsonObject();
                    for (int i = 0, len = callbackBody.size(); i < len; i++) {
                        PolicyParam param = callbackBody.get(i);
                        if (param == null || param.key == null)
                            continue;

                        body.addProperty(param.key, param.value);
                    }
                    sb.append(body.toString());
                } else {
                    for (int i = 0, len = callbackBody.size(); i < len; i++, sb.append(i < len ? "&" : "")) {
                        PolicyParam param = callbackBody.get(i);
                        if (param == null)
                            continue;

                        String str = param.format();
                        if (str == null)
                            continue;

                        sb.append(str);
                    }
                }
            }

            json.addProperty("callbackBody", sb.toString());
            String policy = json.toString();

            return new PutPolicyForCallback(policy, Base64.getUrlEncoder().encodeToString(
                    policy.getBytes(Charset.forName("UTF-8"))));
        }
    }
}
