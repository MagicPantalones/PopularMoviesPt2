package io.magics.popularmovies;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.facebook.stetho.Stetho;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieListsActivity extends AppCompatActivity {

    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.sliding_tabs)
    TabLayout mTabLayout;
    @BindView(R.id.up_fab)
    FloatingActionButton mUpFab;
    List<UpFabListener> mUpFabListeners = new ArrayList<>();

    public interface UpFabListener{
        void upFabUp();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_lists);
        ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);

        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new MovieListsPagerAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(new SimpleOnPageChangeListener(){
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING && mUpFab.getVisibility() == View.VISIBLE) mUpFab.hide();
                else if (state == ViewPager.SCROLL_STATE_IDLE && mUpFab.getVisibility() != View.VISIBLE) mUpFab.show();
                super.onPageScrollStateChanged(state);
            }

        });

        mTabLayout.setupWithViewPager(mViewPager);
        mUpFab.setOnClickListener(v -> mUpFabListeners.get(mTabLayout.getSelectedTabPosition()).upFabUp());

    }

    public void registerUpFab(UpFabListener upFabListener){
        mUpFabListeners.add(upFabListener);
    }

    public void unRegisterUpFab(UpFabListener upFabListener){
        if (!mUpFabListeners.isEmpty()) mUpFabListeners.remove(upFabListener);
    }

}
