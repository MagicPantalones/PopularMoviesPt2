package io.magics.popularmovies;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentListTabLayout extends Fragment {

    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.sliding_tabs)
    TabLayout mTabLayout;
    @BindView(R.id.up_fab)
    FloatingActionButton mUpFab;

    private List<UpFabListener> mUpFabListeners = new ArrayList<>();
    private Unbinder mUnbinder;

    public interface UpFabListener{
        void upFabUp();
    }

    public FragmentListTabLayout() {
        // Required empty public constructor
    }

    public static FragmentListTabLayout instantiateFragment(){
        return new FragmentListTabLayout();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list_adapter_tablayout, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        mViewPager.setOffscreenPageLimit(3);

        mViewPager.setAdapter(new MovieListsPagerAdapter(getChildFragmentManager()));

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING && mUpFab.getVisibility() == View.VISIBLE) mUpFab.hide();
                else if (state == ViewPager.SCROLL_STATE_IDLE && mUpFab.getVisibility() != View.VISIBLE) mUpFab.show();
                super.onPageScrollStateChanged(state);
            }

        });

        mTabLayout.setupWithViewPager(mViewPager);
        mUpFab.setOnClickListener(v -> mUpFabListeners.get(mTabLayout.getSelectedTabPosition()).upFabUp());

        return root;
    }

    @Override
    public void onDestroy() {
        mUnbinder.unbind();
        super.onDestroy();
    }

    public void registerUpFab(UpFabListener upFabListener){
        mUpFabListeners.add(upFabListener);
    }

    public void unRegisterUpFab(UpFabListener upFabListener){
        if (!mUpFabListeners.isEmpty()) mUpFabListeners.remove(upFabListener);
    }

}
