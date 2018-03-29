package com.zac4j.yoda.ui.home.timeline;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.sso.AccessTokenKeeper;
import com.zac4j.yoda.R;
import com.zac4j.yoda.data.model.Weibo;
import com.zac4j.yoda.ui.adapter.TimelinesAdapter;
import com.zac4j.yoda.ui.base.BaseFragment;
import com.zac4j.yoda.ui.listener.EndlessRecyclerViewScrollListener;
import com.zac4j.yoda.ui.weibo.detail.WeiboDetailActivity;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Weibo list page
 * Created by zac on 3/17/2017.
 */

public class TimelineFragment extends BaseFragment implements TimelineView {

    // WebService default weibo count is 20 as well.
    public static final int DEFAULT_WEIBO_COUNT = 8;
    private final int mRequestCount = DEFAULT_WEIBO_COUNT;
    @Inject
    TimelinePresenter mPresenter;
    @Inject
    TimelinesAdapter mTimelineAdapter;
    @BindView(R.id.swipe_weibo_list_container)
    SwipeRefreshLayout mSwipeContainer;
    @BindView(R.id.recycler_weibo_list)
    RecyclerView mWeiboListView;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.error_view)
    View mErrorView;
    private int mRequestPage = 1;
    private String mToken; // user token
    private EndlessRecyclerViewScrollListener mScrollListener;

    public static TimelineFragment newInstance() {
        return new TimelineFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weibo_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getFragmentComponent().inject(this);
        ButterKnife.bind(this, view);
        mPresenter.attach(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mWeiboListView.setLayoutManager(layoutManager);
        mWeiboListView.setAdapter(mTimelineAdapter);
        mTimelineAdapter.setOnItemClickListener(weibo -> goToWeiboDetail(getContext(), weibo));

        Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(getContext());
        mToken = accessToken.getToken();
        Timber.d("Token: " + mToken + "\r\n" + "UID: >> " + accessToken.getUid());
        mScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                mRequestPage = page;
                mPresenter.getTimeline(mToken, mRequestCount, mRequestPage);
            }
        };
        mWeiboListView.addOnScrollListener(mScrollListener);

        mSwipeContainer.setOnRefreshListener(this::refreshPage);

        mSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light, android.R.color.holo_orange_light,
            android.R.color.holo_red_light);

        refreshPage();
    }

    @Override
    public void onStop() {
        mSwipeContainer.setRefreshing(false);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detach();
        mWeiboListView.removeOnScrollListener(mScrollListener);
    }

    @Override
    public void showProgress(boolean show) {
        if (mProgressBar == null || mSwipeContainer.isRefreshing()) {
            return;
        }

        mProgressBar.setVisibility(show ? VISIBLE : GONE);
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showEmptyView(boolean show) {
        mErrorView.setVisibility(show ? VISIBLE : GONE);
    }

    @Override
    public void showTimeline(List<Weibo> weiboList) {
        if (mRequestPage == 1) { // while refresh the list.
            if (mScrollListener != null) {
                mScrollListener.resetState();
            }
            mTimelineAdapter.clear();
        }

        mTimelineAdapter.addWeiboList(weiboList);

        mSwipeContainer.setRefreshing(false);
        mWeiboListView.setVisibility(VISIBLE);
    }

    @Override
    public void showRefresh(boolean show) {
        mSwipeContainer.setRefreshing(show);
    }

    @Override
    public boolean isRefreshing() {
        return mSwipeContainer != null && mSwipeContainer.isRefreshing();
    }

    /**
     * Refresh current page
     */
    private void refreshPage() {
        mRequestPage = 1;
        mPresenter.getTimeline(mToken, DEFAULT_WEIBO_COUNT, mRequestPage);
    }

    /**
     * 跳转到微博详情页
     *
     * @param context context
     * @param weibo 微博实例
     */
    private void goToWeiboDetail(Context context, Weibo weibo) {
        if (weibo == null) {
            return;
        }
        Intent intent = new Intent(context, WeiboDetailActivity.class);
        intent.putExtra(WeiboDetailActivity.EXTRA_WEIBO, weibo);
        context.startActivity(intent);
    }
}
