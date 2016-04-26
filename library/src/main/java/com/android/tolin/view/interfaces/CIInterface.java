package com.android.tolin.view.interfaces;

import android.view.View;

import com.android.tolin.view.TolinRefreshViewGroup;


/**
 * Created by Administrator on 2016/4/19.
 */
public interface CIInterface {
    void addHeader(View view);


    /**
     * 添加滚动列表控件。
     *
     * @param view
     */
    void addContent(View view);


    FooterInterfaces getFooterInfo();

    View getHeader();

    void setOnLoadMoreListener(TolinRefreshViewGroup.OnLoadMoreListener onLoadMoreListener);

    /**
     * 加载更多完成后调用。
     * @param  isComplete true:加载完成 ; false:开始加载。
     */
    void completeLoadMore(boolean isComplete);

    /**
     * 刷新完成后调用。
     */
    void completeRefresh();
}
