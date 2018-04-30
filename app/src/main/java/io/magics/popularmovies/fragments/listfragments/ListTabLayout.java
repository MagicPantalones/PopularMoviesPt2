package io.magics.popularmovies.fragments.listfragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.MovieListsActivity;
import io.magics.popularmovies.R;
import io.magics.popularmovies.fragments.MovieListsPagerAdapter;

public class ListTabLayout extends Fragment {

    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.sliding_tabs)
    TabLayout mTabLayout;
    @BindView(R.id.app_bar_tab_layout)
    AppBarLayout mAppBar;
    @BindView(R.id.iv_app_bar_back)
    ImageView mAppBarBack;

    Unbinder mUnbinder;

    MovieListsPagerAdapter mAdapter;

    FragmentManager mAppFragManager;

    TabLayoutPageEvents mPageEventsListener;

    public ListTabLayout() {
        // Required empty public constructor
    }

    public static ListTabLayout newInstance() {
        return new ListTabLayout();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_list_tab_layout, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mAppFragManager = getChildFragmentManager();


        mAdapter = new MovieListsPagerAdapter(mAppFragManager);

        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                mPageEventsListener.onPageDrag(state);
                super.onPageScrollStateChanged(state);
            }
        });

        mTabLayout.setupWithViewPager(mViewPager);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        if (context instanceof TabLayoutPageEvents) {
            mPageEventsListener = (TabLayoutPageEvents) context;
        }
        super.onAttach(context);
    }

    public void notifyUpFabPressed() {
        mAdapter.getOneListFragment(mTabLayout.getSelectedTabPosition()).scrollRecyclerViewToTop();
    }

    public interface TabLayoutPageEvents {
        void onPageDrag(int state);
    }

}
