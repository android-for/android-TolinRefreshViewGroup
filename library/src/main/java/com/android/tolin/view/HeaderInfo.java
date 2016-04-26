package com.android.tolin.view;

import android.content.Context;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.android.tolin.view.interfaces.HeaderInterfaces;

import java.text.SimpleDateFormat;

import tolin.android.com.tolinrefreshviewgroup.R;


/**
 * Created by Administrator on 2016/4/20.
 */
public class HeaderInfo implements HeaderInterfaces, TolinRefreshViewGroup.OnHeaderRefreshActionListener {
    private static final String TAG = HeaderInfo.class.getName();
    private View header;
    private ImageView ivReImg;
    private ProgressBar pb;
    private TextView tvTitle;
    private TextView tvTimes;
    private long lastTimes = 0;
    //    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    private RotateAnimation mFAnimation;
    private RotateAnimation mRFAnimation;

    public HeaderInfo(Context context) {
        initView(context);
    }

    private void initView(Context context) {
        header = View.inflate(context, R.layout.auto_refresh_default_header, null);
        ivReImg = (ImageView) header.findViewById(R.id.ivReImg);
        pb = (ProgressBar) header.findViewById(R.id.pb);
        tvTitle = (TextView) header.findViewById(R.id.tvTitle);
        tvTimes = (TextView) header.findViewById(R.id.tvTimes);
        pb.setVisibility(View.GONE);
    }


    @Override
    public void onStartRefreshActionListener() {
//        Log.d(TAG, "onStartRefreshActionListener--->");
        changeViewVisibility(true);
    }


    /**
     * @param isVisible true：显示箭头，隐藏进度progressbar
     */
    private void changeViewVisibility(boolean isVisible) {
        if (isVisible) {
            pb.setVisibility(View.GONE);
            ivReImg.setVisibility(View.VISIBLE);
            tvTitle.setText("下滑刷新");
            if (lastTimes > 0) {
                tvTimes.setText("上次刷新：" + simpleDateFormat.format(lastTimes));
                tvTimes.setVisibility(View.VISIBLE);
            } else {
                tvTimes.setVisibility(View.GONE);
            }
        } else {
            pb.setVisibility(View.VISIBLE);
            ivReImg.clearAnimation();
            ivReImg.setVisibility(View.GONE);
            tvTitle.setText("刷新中……");
        }
    }

    @Override
    public void onCanRefreshActionListener(boolean isCanRefresh) {
//        Log.d(TAG, "onCanRefreshActionListener--->" + isCanRefresh);
        startRefreshActionAnim(isCanRefresh);
    }

    @Override
    public void onPrepareRefreshActionListener() {
//        Log.d(TAG, "onPrepareRefreshActionListener");
        changeViewVisibility(false);
    }

    /**
     * 执行箭头旋转动画。
     */

    private void startRefreshActionAnim(boolean isCanRefresh) {
        buildAnimation();
        ivReImg.clearAnimation();
        if (isCanRefresh) {
            ivReImg.setAnimation(mFAnimation);
            mFAnimation.start();
            tvTitle.setText("释放刷新");
        } else {
            tvTitle.setText("下滑刷新");
            ivReImg.setAnimation(mRFAnimation);
            mRFAnimation.start();
        }
    }

    private void buildAnimation() {
        if (mFAnimation != null && mRFAnimation != null) {
            return;
        }
        mFAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFAnimation.setInterpolator(new LinearInterpolator());
        mFAnimation.setDuration(100);
        mFAnimation.setFillAfter(true);

        mRFAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mRFAnimation.setInterpolator(new LinearInterpolator());
        mRFAnimation.setDuration(100);
        mRFAnimation.setFillAfter(true);
    }

    @Override
    public void onCompleteRefreshActionListener() {
        lastTimes = System.currentTimeMillis();
        pb.clearAnimation();
//        Log.d(TAG, "onCompleteRefreshActionListener--->" + lastTimes);
    }

    @Override
    public void onCancelRefreshActionListener() {
//        Log.d(TAG, "onCancelRefreshActionListener");
    }

    @Override
    public View getHeaderView() {
        return header;
    }
}
