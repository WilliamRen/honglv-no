package com.yoyiyi.honglv.ui.fragment.another.week;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.orhanobut.logger.Logger;
import com.yoyiyi.honglv.R;
import com.yoyiyi.honglv.base.BaseFragment;
import com.yoyiyi.honglv.bean.WeekUpdate;
import com.yoyiyi.honglv.network.manager.HttpManager;
import com.yoyiyi.honglv.ui.widget.empty.EmptyLayout;
import com.yoyiyi.honglv.utils.TDevice;
import com.yoyiyi.honglv.utils.WeekUtil;

import net.qiujuer.genius.ui.widget.Loading;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 一周更新
 * Created by yoyiyi on 2016/10/25.
 */
public class WeekFragment extends BaseFragment {

    private static final String INDEX = "RankFragment_Index";
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.sliding_tabs)
    SegmentTabLayout mSlidingTabs;
    @BindView(R.id.fl_week)
    FrameLayout mFlWeek;
    @BindView(R.id.empty)
    EmptyLayout mEmpty;
    @BindView(R.id.loading)
    Loading mLoading;


    private List<WeekUpdate> mWeekUpdates = new ArrayList<>();
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private int mCurrentTab;
    private int mCurentWeek;

    public static WeekFragment newInstance(String index) {
        Bundle bundle = new Bundle();
        bundle.putString(INDEX, index);
        WeekFragment fragment = new WeekFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_week;
    }

    @Override
    protected void finishCreateView(Bundle state) {
        isPrepared = true;
        loadData();
    }

    @Override
    protected void loadData() {
        if (!isPrepared) return;
        requestData();
        isPrepared = false;
    }

    @Override
    protected void initWidget(View root) {
        mLoading.setAutoRun(true);
        mLoading.start();
        initToolbar();
    }

    private void initToolbar() {
        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        mToolbar.setTitle("一周更新");
        mToolbar.setNavigationOnClickListener(v -> getActivity().finish());
    }

    private void requestData() {
        clearData();
        mLoading.setAutoRun(true);
        mLoading.start();
        new Handler().post(() ->
                doHttpxConnection()
        );
    }

    private void clearData() {
        mWeekUpdates.clear();
    }

    private void finishTask() {
        mLoading.stop();
        mLoading.setVisibility(View.GONE);
        mEmpty.setVisibility(View.GONE);
    }

    private void initFragment() {
        if (mWeekUpdates != null && mWeekUpdates.size() != 0) {
            for (int i = 0; i < mWeekUpdates.size(); i++) {
                mFragments.add(WeekDetailFragment.newInstance(i, mWeekUpdates.get(i)));
            }
        }
        getChildFragmentManager().beginTransaction()
                .add(R.id.fl_week, mFragments.get(0))
                .show(mFragments.get(0)).commit();
        initTab();


    }

    private void initTab() {
        mSlidingTabs.setTabData(TDevice.getStringArray(R.array.week_arrays));
        //  mCurrentTab = mSlidingTabs.getCurrentTab();
        initWeekData();
        mSlidingTabs.setCurrentTab(mCurrentTab);
        getChildFragmentManager().beginTransaction()
                .add(R.id.fl_week, mFragments.get(mCurrentTab))
                .show(mFragments.get(mCurrentTab))
                .commit();
        mSlidingTabs.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                FragmentTransaction bt =
                        getChildFragmentManager().beginTransaction();
                bt.hide(mFragments.get(mCurrentTab));
                if (!mFragments.get(position).isAdded()) {
                    bt.add(R.id.fl_week, mFragments.get(position));
                }
                bt.show(mFragments.get(position)).commit();
                mCurrentTab = position;
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
    }

    private void initWeekData() {
        String week = WeekUtil.getWeek();
        switch (week) {
            case "日":
                mCurrentTab = 6;
                break;
            case "一":
                mCurrentTab = 0;
                break;
            case "二":
                mCurrentTab = 1;
                break;
            case "三":
                mCurrentTab = 2;
                break;
            case "四":
                mCurrentTab = 3;
                break;
            case "五":
                mCurrentTab = 4;
                break;
            case "六":
                mCurrentTab = 5;
                break;
        }
    }


    private void showEmptyView() {
        mEmpty.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.GONE);
        mEmpty.setEmptyTv("加载错误啦！！");
        mEmpty.setEmptyIv(R.drawable.ic_empty_error);
        mEmpty.setOnItemClickLisener(() -> {
                    mLoading.start();
                    mLoading.setVisibility(View.VISIBLE);
                    mEmpty.setVisibility(View.GONE);
                    requestData();
                }
        );
    }

    private void doHttpxConnection() {
        HttpManager.getHttpManager().getHttpService()
                .getWeekUpdate().compose(bindToLifecycle())
                .map(weekUpdates -> {
                    if (weekUpdates != null && weekUpdates.size() != 0) {
                        return weekUpdates;
                    }
                    return null;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(weekUpdates -> {
                    mWeekUpdates.addAll(weekUpdates);
                    finishTask();
                    initFragment();
                }, e -> {
                    showEmptyView();
                    Logger.d("加载错误");
                });
    }
}
