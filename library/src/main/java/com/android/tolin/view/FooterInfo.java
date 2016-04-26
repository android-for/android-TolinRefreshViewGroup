package com.android.tolin.view;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.tolin.view.interfaces.FooterInterfaces;

import tolin.android.com.tolinrefreshviewgroup.R;


/**
 * Created by Administrator on 2016/4/20.
 */
public class FooterInfo implements FooterInterfaces, View.OnClickListener {
    private static final String TAG = FooterInfo.class.getName();
    private OnClickLoadMoreListener onClickLoadMoreListener;
    private View footer;
    private LinearLayout moreLL;
    private TextView tvFooterHint;
    private ProgressBar pbFooter;

    public FooterInfo(Context context, OnClickLoadMoreListener onClickLoadMoreListener) {
        this.onClickLoadMoreListener = onClickLoadMoreListener;
        initView(context);
    }

    private void initView(Context context) {
        footer = View.inflate(context, R.layout.auto_loadmore_default_footer, null);
        moreLL = (LinearLayout) footer.findViewById(R.id.moreLL);
        tvFooterHint = (TextView) footer.findViewById(R.id.tvFooterHint);
        pbFooter = (ProgressBar) footer.findViewById(R.id.pbFooter);
        tvFooterHint.setOnClickListener(this);
        defaultOnclickLoadMore();
    }


    @Override
    public View getFooterView() {
        return footer;
    }

    @Override
    public void setCompleteLoad() {
        defaultOnclickLoadMore();
    }

    @Override
    public void startMoreLoading() {
        tvFooterHint.setText("加载中……");
        pbFooter.setVisibility(View.VISIBLE);
        tvFooterHint.setEnabled(false);
//        Log.d(TAG, "加载中……");
    }

    @Override
    public void defaultOnclickLoadMore() {
        tvFooterHint.setText("点击加载更多");
        pbFooter.setVisibility(View.GONE);
        tvFooterHint.setEnabled(true);
//        Log.d(TAG, "点击加载更多……");

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvFooterHint) {
            if (onClickLoadMoreListener == null) {
                return;
            }
            onClickLoadMoreListener.onClickLoadMoreListener();
        }
    }

    public interface OnClickLoadMoreListener {
        void onClickLoadMoreListener();
    }
}
