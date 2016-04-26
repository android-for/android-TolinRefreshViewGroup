package com.android.tolin.view.interfaces;

import android.view.View;

/**
 * Created by Administrator on 2016/4/22.
 */
public interface FooterInterfaces {
    View getFooterView();

    /**
     * 设置加载更多完成。
     */
    void setCompleteLoad();

    /**
     * 开始加载更多。
     */
    void startMoreLoading();

    /**
     * 默认点击加载。
     */
    void defaultOnclickLoadMore();
}
