package com.android.tolin.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.android.tolin.view.interfaces.FooterInterfaces;
import com.android.tolin.view.interfaces.TolinAdapterInterfaces;

import java.util.ArrayList;


/**
 * RecyclerView 适配器父类。
 * 注：getItemCount获取到的count包含加载更多页脚。（前提是开启了加载更多功能：AutoViewGroup.setEnableFooter(true)）
 */
public abstract class TolinRecyclerAdapter<TH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter implements TolinAdapterInterfaces {
    public static final int ItemViewFooterType = 2;//加载更多类型标记。
    private ArrayList datas = new ArrayList();
    private FooterInterfaces footerInterfaces;


    @Override
    public void addLoadMoreFooter(FooterInterfaces footerInterfaces) {
        if (datas.size() > 0) {
            datas.remove(this.footerInterfaces);
        }
        datas.add(footerInterfaces);
        this.footerInterfaces = footerInterfaces;
        notifyItemInserted(datas.size() - 1);
    }

    @Override
    public FooterInterfaces getFooterInfo() {
        return footerInterfaces;
    }


    @Override
    public int getItemViewType(int position) {
        if (getItemCount() - 1 == position) {
            return ItemViewFooterType;
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (footerInterfaces != null && viewType == ItemViewFooterType) {
            return new FooterViewHolder(footerInterfaces.getFooterView());
        }
        return getCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (footerInterfaces != null && position == getData().size() - 1) {
            return;
        }
        onTolinBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public void addData(ArrayList list) {
        int count = 0;
        if (footerInterfaces != null && datas.size() > 0) {
            count = datas.size() - 1;
            datas.addAll(count, list);
            return;
        }
        datas.addAll(list);
    }

    public ArrayList getData() {
        return datas;
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
