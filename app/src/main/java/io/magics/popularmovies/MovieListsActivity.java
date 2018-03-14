package io.magics.popularmovies;

import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.facebook.stetho.Stetho;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieListsActivity extends AppCompatActivity
    implements MovieListFragment.MovementCatcher{

    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.sliding_tabs)
    TabLayout mTabLayout;
    @BindView(R.id.up_fab)
    FloatingActionButton mUpFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_lists);
        ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);

        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new MovieListsPagerAdapter(getSupportFragmentManager()));

        mTabLayout.setupWithViewPager(mViewPager);


    }

    @Override
    public void fragmentActionListener(MovieListFragment.FragmentAction action) {
        switch (action){
            case ACTION_SCROLLING_UP:
                mUpFab.show();
                break;
            case ACTION_SCROLLING_DOWN:
                mUpFab.hide();
                break;
            case ACTION_SCROLLED_HORIZONTAL:
                mUpFab.show();
                break;
            case ACTION_SCROLLING_HORIZONTAL:
                mUpFab.hide();
                break;
            default:
                mUpFab.show();
                break;
        }
    }
}
