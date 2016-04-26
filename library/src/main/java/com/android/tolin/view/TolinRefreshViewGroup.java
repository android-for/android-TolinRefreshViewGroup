package com.android.tolin.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.android.tolin.view.adapter.TolinRecyclerAdapter;
import com.android.tolin.view.interfaces.CIInterface;
import com.android.tolin.view.interfaces.FooterInterfaces;


/**
 * Created by Administrator on 2016/4/19.
 */
public class TolinRefreshViewGroup extends ViewGroup implements CIInterface, FooterInfo.OnClickLoadMoreListener {
    /**
     * 触发刷新事件的下滑基数。（下拉刷新头的距离超过头高度多少倍时，才触发刷新事件。）
     */
    private static final float HeaderHieghtBase = 1.0f;
    /**
     * 延迟刷新时间。
     */
    private long PostDelayRefreshTimes = 1000;
    /**
     * 延迟加载更多时间。
     */
    private long PostDelayLoadMoreTime = 1000;

    /**
     * 滑动刷新阻尼系数。
     */
    private float RefreshDamping = 3f;
    /**
     * 是否开启刷新功能。默认为不开启。
     */
    private boolean enHeaderFlag = true;
    /**
     * 是否开启加载更多功能。
     */
    private boolean enFooterFlag = true;
    private View header;
    private View footer;
    private Handler handler;
    private RecyclerView mContent;
    private HeaderInfo headerInfo;
    //    private FooterInfo footerInfo;
    private boolean isSaveFirst = true;//第1次保存，只保存一次。
    private boolean isRefreshing = false;//是否刷新中，默认false.
    private ValueAnimator valueAnimator;
    private OnRefreshListener onRefreshListener;
    private OnLoadMoreListener onLoadMoreListenern;
    private boolean isStartRefresh = false;//触发刷新事件。默认为false;
    private Handler mHandler;
    private boolean isFirstOpenView = false;
    private boolean firstMoveHint = true;
    private boolean isLoadMoreing = false;//是否加载中。
    private TolinRecyclerAdapter tolinRecyclerAdapter;


    public TolinRefreshViewGroup(Context context) {
        super(context);
        initView(context);
    }


    public TolinRefreshViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TolinRefreshViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TolinRefreshViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int myWidth = -1;
        int myHeight = -1;

        int widthSize = MeasureSpec.getSize(widthMeasureSpec); //size可以自己定义，也可以通过MeasureSpec.getSize的方式获取原始大小。（一般用后者）
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);  //mode是根据layout_width的参数决定的，也就是layout_width="match_parent|wrap_content"或者指定dp值，不能自己乱写
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode != MeasureSpec.UNSPECIFIED) {
            myWidth = widthSize;
        }
        if (heightMode != MeasureSpec.UNSPECIFIED) {
            myHeight = heightSize;
        }
        for (int index = 0; index < getChildCount(); index++) {
//            Log.d("onMeasure--->", "" + index);
            getChildAt(index).measure(widthMeasureSpec, heightMeasureSpec);  //测量子控件的宽高。
//            Log.d("测量尺寸","getMeasuredHeight--->"+getChildAt(index).getMeasuredHeight());
        }
        setMeasuredDimension(myWidth, myHeight);//在最后必须调用该函数
//        saveHeadresOldInfo();

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int headerHeight = 0;
        if (header != null) {
            headerHeight = header.getMeasuredHeight();
            header.layout(l, t - headerHeight, r, b);
        }
        if (mContent != null) {
            mContent.layout(l, t, r, b);
        }
    }

    private boolean dispatchSuperTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    float oldX = 0;
    float oldY = 0;
    float moveX = 0;
    float moveY = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        RecyclerView.LayoutManager layoutManager = mContent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            boolean canScrollY = linearLayoutManager.canScrollVertically();
            int lastC = linearLayoutManager.findLastCompletelyVisibleItemPosition();
            int firstC = linearLayoutManager.findFirstCompletelyVisibleItemPosition();//当前屏幕中显示完整的第1个item在adapter中的位置。
            int count = linearLayoutManager.getItemCount();

//            刷新数据;
            if (!isLoadMoreing && (isRefreshing || firstC == -1 || firstC == 0)) {
                boolean refreshFlag = refreshAction(ev);
                if (!refreshFlag) {
                    return dispatchSuperTouchEvent(ev);
                }
                return refreshFlag;
            } else if (count > 0 && lastC == (count - 1) && !isLoadMoreing) {  //加载更多数据。
                boolean loadMoreFlag = loadMoreAction(ev);
                if (!loadMoreFlag) {
                    return dispatchSuperTouchEvent(ev);
                }
                return loadMoreFlag;
            }
        }
        return dispatchSuperTouchEvent(ev);
    }

    /**
     * 加载更多数据入口。
     */
    private boolean loadMoreAction(MotionEvent ev) {
//        Log.d("滚动状态：", "加载更多数据--->");
        if (!isEnableFooter() || isLoadMoreing) {
            return false;
        }
        onLoadMore();
        return true;
    }


    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return super.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    float reOldX = 0;
    float reOldY = 0;
    float reStopX = 0;
    float reStopY = 0;
    float reCurrX = 0;
    float reCurrY = 0;
    float finY = 0;
    float headerMesH = 0;
    float scrollY = 0;

    /**
     * 刷新数据动作入口。
     *
     * @return true:消耗该事件，false：分发该事件下去。
     */
    private synchronized boolean refreshAction(MotionEvent ev) {
        if (!isEnableHeader() || isStartRefresh) {
            return false;
        }
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isRefreshing) {
                    return false;
                }
                reOldX = ev.getX();
                reOldY = ev.getY();
//                Log.d("滚动状态：", "按下---reOldX-->" + reOldX + "--reOldY-->" + reOldY);
                headerInfo.onStartRefreshActionListener();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                reStopX = ev.getX();
                reStopY = ev.getY();


//                Log.d("刷新流程", "scrollY---->" + getScrollY() + "--headeH--->" + header.getMeasuredHeight() * HeaderHieghtBase);
                if (scrollY > headerMesH) {
                    isStartRefresh = true;
                } else {
                    isStartRefresh = false;
                }
                if (isRefreshing) {
                    refreshStart(finY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                reCurrX = ev.getX();
                reCurrY = ev.getY();
                float computY = reOldY - reCurrY;
//                Log.d("滚动状态：", "移动中---reCurrX-->" + reCurrX + "--reCurrY-->" + reCurrY);
//                Log.d("滚动状态：", "刷新划动距离" + computY);
                finY = computY;
                if (isRefreshing = true && computY <= 0) {
//                小于0，表示向下滑动刷新。
                    finY = (float) (finY * (RefreshDamping / 10));
//                    Log.d("滚动状态：", "刷新ing" + finY);
                    scrollTo(0, (int) finY);

                    scrollY = Math.abs(finY);
                    headerMesH = header.getMeasuredHeight() * HeaderHieghtBase;
                    if (scrollY > headerMesH && firstMoveHint) {
                        headerInfo.onCanRefreshActionListener(firstMoveHint);
                        firstMoveHint = false;
                    } else if (scrollY < headerMesH && !firstMoveHint) {
                        headerInfo.onCanRefreshActionListener(firstMoveHint);
                        firstMoveHint = true;
                    }
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * 刷新开始。
     */
    private void refreshStart(float endScrollDis) {
        if (isStartRefresh) {
            resetHeadler();
//            Log.d("刷新流程", "回弹头1.2倍----->" + firstScrollY);
        } else {
            resetHeader(endScrollDis);
//            Log.d("刷新流程", "回弹未触发----->" + endScrollDis);
        }
    }

    /**
     * 执行刷新数据的回弹。
     */
    private void resetHeadler() {
        headerInfo.onPrepareRefreshActionListener();

        ValueAnimator valueAnimator = new ValueAnimator();
        float headY = header.getY();
        final float scrollY = getScrollY();
        final float scrolDis = scrollY - headY;
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
        decelerateInterpolator.getInterpolation(scrolDis);
        valueAnimator.setInterpolator(decelerateInterpolator);
        valueAnimator.setFloatValues(scrolDis);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                value = scrollY - value;
                scrollTo(0, (int) value);
//                Log.d("滚动状态：", "回弹一半-->animation.getAnimatedValue()--> " + animation.getAnimatedValue() + "-->value-->" + value + "--->scrollY--> " + scrollY + " --scrolDis---> " + scrolDis);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onRefresh();
                    }
                }, PostDelayRefreshTimes);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.start();
    }


    /**
     * 恢复刷新头位置。
     *
     * @param endScrollDis 必须传递为正数。
     */
    private void resetHeader(float endScrollDis) {
        ValueAnimator valueAnimator = new ValueAnimator();
        if (endScrollDis >= 0) {
            return;
        }
        Log.d("滚动状态：", "resetHeader--->" + endScrollDis);
        endScrollDis = Math.abs(endScrollDis);
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
        decelerateInterpolator.getInterpolation(endScrollDis);
        valueAnimator.setInterpolator(decelerateInterpolator);
        valueAnimator.setFloatValues(endScrollDis);
        final float finalEndScrollDis = endScrollDis;
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                value = finalEndScrollDis - value;
                scrollTo(0, (int) -value);
//                Log.d("滚动状态：", "回弹完全-->" + value + "--->endScrollDis-->" + finalEndScrollDis);
//                Log.d("滚动状态：", "滚动距离-getScrolY--->" + getScrollY());
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isStartRefresh) {
                    headerInfo.onCompleteRefreshActionListener();
                } else {
                    headerInfo.onCancelRefreshActionListener();
                }
                resetRefreshFlag();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                resetRefreshFlag();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.start();
    }

    /**
     * 刷新结束。重置刷新标记。
     */
    private void resetRefreshFlag() {

        isRefreshing = false;
        isStartRefresh = false;
    }

    private void initView(Context context) {
        mHandler = new Handler();
        if (isEnableHeader()) {
            headerInfo = new HeaderInfo(context);
            addHeader(headerInfo.getHeaderView());
        }
//        第2个添加。不能放在footer后面添加。
        if (mContent == null) {
            mContent = new RecyclerView(context);
            mContent.setLayoutParams(new RecyclerView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        addContent(mContent);
    }

    private void testHandlerExe() {
        if (handler == null) {
            handler = new Handler();
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollBy(0, -10);
                testHandlerExe();
//                Log.d("scrolTo:", "-------------");
            }
        }, 100);
    }

    @Override
    public void addHeader(View view) {
        if (view == null) {
            return;
        }
        addView(view);
        header = view;
    }

    public void addFooter(FooterInterfaces footerInterfaces) {
        if (tolinRecyclerAdapter == null) {
            return;
        }
        tolinRecyclerAdapter.addLoadMoreFooter(footerInterfaces);
    }

    public TolinRecyclerAdapter getAdapter() {
        if (mContent == null) {
            return null;
        }
        return (TolinRecyclerAdapter) mContent.getAdapter();
    }

    public void setAdapter(TolinRecyclerAdapter tolinRecyclerAdapter) {
        if (mContent == null) {
            return;
        }
        this.tolinRecyclerAdapter = tolinRecyclerAdapter;
        if (isEnableFooter()) {
            addFooter(new FooterInfo(mContent.getContext(), this));
        }
        mContent.setAdapter(tolinRecyclerAdapter);
        tolinRecyclerAdapter.notifyItemInserted(tolinRecyclerAdapter.getItemCount() - 1);

    }


    /**
     * 需要放在addView(header)后添加。
     */
    @Override
    public void addContent(View view) {
        addView(view);
    }

    @Override
    public FooterInterfaces getFooterInfo() {
        return getAdapter().getFooterInfo();
    }


    @Override
    public View getHeader() {
        return header;
    }

    @Override
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListenern = onLoadMoreListener;
    }

    @Override
    public void completeLoadMore(boolean isComplete) {
        if (!isEnableFooter()) {
            return;
        }
        if (isComplete) {
            isLoadMoreing = false;
            getFooterInfo().defaultOnclickLoadMore();
        } else {
            isLoadMoreing = true;
            getFooterInfo().startMoreLoading();
        }
    }

    @Override
    public void completeRefresh() {
        if (isRefreshing) {
            resetHeader(header.getY());
        }
    }

    public boolean isEnableHeader() {
        return enHeaderFlag;
    }

    /**
     * 开启/关闭下拉头刷新功能。
     *
     * @param enableHeader
     */
    public void setEnableHeader(boolean enableHeader) {
        this.enHeaderFlag = enableHeader;
    }

    public boolean isEnableFooter() {
        return enFooterFlag;
    }

    /**
     * 开启/关闭加载更多功能。
     *
     * @param enableFooter
     */
    public void setEnableFooter(boolean enableFooter) {
        this.enFooterFlag = enableFooter;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.mContent = recyclerView;
    }

    public RecyclerView getRecyclerView() {
        return mContent;
    }

    public HeaderInfo getHeaderInfo() {
        return headerInfo;
    }

//    public void setHeaderInfo(HeaderInfo headerInfo) {
//        this.headerInfo = headerInfo;
//        addView(headerInfo.getView());
//    }


    private void onLoadMore() {
        try {
            if (onLoadMoreListenern == null) {
                return;
            }
            completeLoadMore(false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onLoadMoreListenern.onLoadMoreListener();
                }
            }, PostDelayLoadMoreTime);
        } catch (Exception e) {
            e.printStackTrace();
            completeLoadMore(true);
        }
    }

    private void onRefresh() {
        try {
            if (onRefreshListener == null) {
                return;
            }
            onRefreshListener.onRefreshListener();
        } catch (Exception e) {
            e.printStackTrace();
            resetHeader(header.getY());
        } finally {
//            Log.d("刷新状态", "刷新回调成功");
        }
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    @Override
    public void onClickLoadMoreListener() {
        onLoadMore();
    }


    /**
     * 加载更多回调事件。
     */
    public interface OnLoadMoreListener {
        /**
         * 加载更多监听器。
         */
        void onLoadMoreListener();
    }

    /**
     * 刷新数据回调监听器。
     */
    public interface OnRefreshListener {
        /**
         * 刷新监听器。
         */
        void onRefreshListener();
    }

    /**
     * 刷新头动作监听器。
     */
    public interface OnHeaderRefreshActionListener {
        /**
         * 开始刷新。
         */
        void onStartRefreshActionListener();

        /**
         * 可以触发刷新事件。
         */
        void onCanRefreshActionListener(boolean isCanRefresh);

        /**
         * 准备刷新。（刷新松开手指与刷新动作之间的那段时间）
         */
        void onPrepareRefreshActionListener();

        /**
         * 刷新动作结束。
         */
        void onCompleteRefreshActionListener();

        /**
         * 取消刷新动作。
         */
        void onCancelRefreshActionListener();
    }
}
