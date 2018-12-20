package cn.ucloud.ufile.http;

import cn.ucloud.ufile.bean.UfileErrorBean;

/**
 * Ufile默认异步回调，指定了错误时的返回类型{@link UfileErrorBean}
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/20 11:27
 */
public abstract class UfileCallback<T> extends BaseHttpCallback<T, UfileErrorBean> {
}
