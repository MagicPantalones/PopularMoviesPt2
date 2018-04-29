package io.magics.popularmovies;

import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.stetho.Stetho;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.fragments.MovieListsPagerAdapter;
import io.magics.popularmovies.fragments.detailfragments.MovieDetailsFragment;
import io.magics.popularmovies.fragments.listfragments.ListFragment;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.networkutils.DataProvider;
import io.magics.popularmovies.utils.AnimationHelper;
import io.magics.popularmovies.utils.MovieUtils;
import io.magics.popularmovies.utils.MovieUtils.ScrollDirection;
import io.magics.popularmovies.viewmodels.AnimationHelperViewModel;
import io.magics.popularmovies.viewmodels.FavListViewModel;
import io.magics.popularmovies.viewmodels.PopListViewModel;
import io.magics.popularmovies.viewmodels.ReviewsViewModel;
import io.magics.popularmovies.viewmodels.TopListViewModel;
import io.magics.popularmovies.viewmodels.TrailersViewModel;

public class MovieListsActivity extends AppCompatActivity implements ListFragment.FragmentListener,
        MovieDetailsFragment.DetailFragInteractionHandler {

    //TODO Add horizontal layout support.
    //TODO Inspect for memoryleaks

    static final String ADAPTER_DATA = "adapterData";

    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.sliding_tabs)
    TabLayout mTabLayout;
    @BindView(R.id.app_bar_main)
    AppBarLayout mAppBar;
    @BindView(R.id.up_fab)
    FloatingActionButton mUpFab;
    @BindView(R.id.iv_app_bar_back)
    ImageView mAppBarBack;
    @BindView(R.id.container_main)
    ViewGroup mMainContainer;

    private static final String DETAIL_FRAGMENT_TAG = "detailFrag";

    MovieListsPagerAdapter mAdapter;
    DataProvider mDataProvider;
    AnimationHelper mAnimationHelper;

    TopListViewModel mTopListVM;
    PopListViewModel mPopListVM;
    FavListViewModel mFavListVM;
    TrailersViewModel mTrailerVm;
    ReviewsViewModel mReviewVm;
    AnimationHelperViewModel mAnimHelperVm;

    Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_lists);
        mUnbinder = ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);

        mTopListVM = ViewModelProviders.of(this).get(TopListViewModel.class);
        mPopListVM = ViewModelProviders.of(this).get(PopListViewModel.class);
        mFavListVM = ViewModelProviders.of(this).get(FavListViewModel.class);
        mTrailerVm = ViewModelProviders.of(this).get(TrailersViewModel.class);
        mReviewVm = ViewModelProviders.of(this).get(ReviewsViewModel.class);
        mAnimHelperVm = ViewModelProviders.of(this).get(AnimationHelperViewModel.class);

        mViewPager.setOffscreenPageLimit(3);
        mAdapter = new MovieListsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING && mUpFab.getVisibility() == View.VISIBLE) mUpFab.hide();
                else if (state == ViewPager.SCROLL_STATE_IDLE && mUpFab.getVisibility() != View.VISIBLE) mUpFab.show();
                super.onPageScrollStateChanged(state);
            }
        });

        mTabLayout.setupWithViewPager(mViewPager);

        mDataProvider = new DataProvider(this, mTopListVM, mPopListVM, mFavListVM,
                mTrailerVm, mReviewVm);

        mDataProvider.initialiseApp();

        mUpFab.setOnClickListener(v -> mAdapter
                .getOneListFragment(mTabLayout.getSelectedTabPosition()).scrollRecyclerViewToTop());

    }

    @Override
    protected void onDestroy() {
        if (mDataProvider != null) mDataProvider.dispose();
        if (mUnbinder != null) mUnbinder.unbind();
        super.onDestroy();
    }

    //Solution to fragment backPressed listener by Hw.Master
    // https://stackoverflow.com/questions/5448653/how-to-implement-onbackpressed-in-fragments
    @Override
    public void onBackPressed() {
        int fragCount = getSupportFragmentManager().getBackStackEntryCount();

        if (fragCount == 0) {
            super.onBackPressed();
        } else {
            mUpFab.show();
            MovieUtils.toggleViewVisibility(View.GONE, mAppBarBack, mTabLayout);
            mAppBar.setBackground(null);
            getSupportFragmentManager().popBackStack();
        }

    }

    public void showMovieDetailsFrag(Movie movie) {

        mDataProvider.setMovieAndFetch(movie);

        mUpFab.hide();
        mAppBarBack.setMinimumHeight(mTabLayout.getHeight());
        mAppBar.setBackgroundResource(R.drawable.bg_toolbar_list);
        MovieUtils.toggleViewVisibility(View.GONE, mTabLayout, mAppBarBack);

        mAppBarBack.setOnClickListener(v -> getSupportFragmentManager().popBackStack());

        MovieDetailsFragment frag = MovieDetailsFragment
                .newInstance(movie, mFavListVM.checkIfFavourite(movie.getMovieId()));

        mMainContainer.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {

                return false;
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_main, frag, DETAIL_FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onMovieViewHolderClicked(RecyclerView recycler, View v, Movie movie) {
        showMovieDetailsFrag(movie);
    }

    @Override
    public void onRecyclerViewScrolled(ScrollDirection scrollDirection) {
        if (scrollDirection == ScrollDirection.SCROLL_DOWN &&
                mUpFab.getVisibility() == View.VISIBLE) mUpFab.hide();
        else if (scrollDirection == ScrollDirection.SCROLL_UP &&
                mUpFab.getVisibility() != View.VISIBLE) mUpFab.show();
    }

    @Override
    public void favFabClicked(Movie movie, Boolean isFavourite) {
        if (isFavourite) mDataProvider.deleteFromFavourites(movie);
        else mDataProvider.addToFavourites(movie);
    }
}


