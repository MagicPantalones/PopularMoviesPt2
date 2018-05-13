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
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.List;
import java.util.Map;

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

    private View mRoot;

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
        mRoot = inflater.inflate(R.layout.fragment_list_tab_layout, container, false);
        mUnbinder = ButterKnife.bind(this, mRoot);

        mAppFragManager = getChildFragmentManager();
        mAdapter = new MovieListsPagerAdapter(mAppFragManager);

        return mRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

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

        postponeEnterTransition();
        prepareTransitions();

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

    private void prepareTransitions(){
        setExitTransition(TransitionInflater.from(getContext())
                .inflateTransition(R.transition.list_exit_transition));

        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

                ListFragment currentList = mAdapter
                        .getOneListFragment(mTabLayout.getSelectedTabPosition());
                RecyclerView listRecycler = currentList.mRecyclerView;

                if (listRecycler == null){
                    return;
                }

                RecyclerView.ViewHolder selectedVh = listRecycler
                        .findViewHolderForAdapterPosition(MovieListsActivity.selectedPosition);

                if (selectedVh == null || selectedVh.itemView == null){
                    return;
                }

                sharedElements.put(names.get(0),
                        selectedVh.itemView.findViewById(R.id.cv_poster_wrapper));
                sharedElements.put(names.get(1), selectedVh.itemView.findViewById(R.id.iv_poster));
                sharedElements.put(names.get(2), mAppBar);
            }
        });

    }

    public interface TabLayoutPageEvents {
        void onPageDrag(int state);
    }

}
