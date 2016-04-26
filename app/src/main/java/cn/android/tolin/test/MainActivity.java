package cn.android.tolin.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.android.tolin.view.TolinRefreshViewGroup;
import com.android.tolin.view.adapter.TolinRecyclerAdapter;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private TolinRefreshViewGroup avg;
    private Apater3 mAdapter;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test2();
    }

    private void test2() {
        mHandler = new Handler();
        avg = (TolinRefreshViewGroup) findViewById(R.id.avg);
        avg.setEnableHeader(true);
        avg.setEnableFooter(true);
        RecyclerView recyclerView = avg.getRecyclerView();
        mAdapter = new Apater3();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        avg.setAdapter(mAdapter);
        avg.setOnRefreshListener(new TolinRefreshViewGroup.OnRefreshListener() {
            @Override
            public void onRefreshListener() {
                Log.d(TAG, "刷新数据");
                mAdapter.getData().clear();
                mAdapter.notifyDataSetChanged();
                postLoadData();
            }
        });
        avg.setOnLoadMoreListener(new TolinRefreshViewGroup.OnLoadMoreListener() {
            @Override
            public void onLoadMoreListener() {
                Log.d(TAG, "加载更多数据");
                postLoadData();
            }
        });
        postLoadData();

    }

    private void postLoadData() {
        avg.completeLoadMore(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, 6000);
    }

    private void loadData() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            arrayList.add(i + "");
        }
        mAdapter.addData(arrayList);
        mAdapter.notifyDataSetChanged();
        avg.completeRefresh();
        avg.completeLoadMore(true);
    }

    private void test1() {
        Intent intent = new Intent("com.demo.SERVICE_DEMO");
        startService(intent);
    }


    private class MViewHodler extends RecyclerView.ViewHolder {
        private TextView tv;


        public MViewHodler(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tvTest);
        }

    }

    private class Apater3 extends TolinRecyclerAdapter {

        @Override
        public void onTolinBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MViewHodler ho = (MViewHodler) holder;
            ho.tv.setText("测试数据-->" + getData().get(position) + "-->" + position);
        }

        @Override
        public RecyclerView.ViewHolder getCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.test_item_lists, null);
            return new MViewHodler(view);
        }
    }
}
