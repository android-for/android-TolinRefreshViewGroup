package com.android.tolin.view.interfaces;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/4/24.
 */
public interface TolinAdapterInterfaces {

    /**
     * 添加加载更多。
     */
    void addLoadMoreFooter(FooterInterfaces footerInterfaces);

    /**
     * 添加数据。
     */
    void addData(ArrayList<Object> list);

    /**
     * 获取数据列表。
     */
    ArrayList getData();

    /**
     * 获取加载更多对象
     */
    FooterInterfaces getFooterInfo();

    /**
     * 内容处理。
     *
     * @param holder   内容item对应的holder
     * @param position
     */
    void onTolinBindViewHolder(RecyclerView.ViewHolder holder, int position);

    /**
     * 用于创建自定义的viewholder对象。
     */
    RecyclerView.ViewHolder getCreateViewHolder(ViewGroup parent, int viewType);

}
