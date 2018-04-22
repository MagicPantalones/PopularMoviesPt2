package io.magics.popularmovies;

import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.stetho.Stetho;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.fragments.MovieListsPagerAdapter;
import io.magics.popularmovies.fragments.detailfragments.MovieDetailsFragment;
import io.magics.popularmovies.fragments.listfragments.ListFavouritesFragment;
import io.magics.popularmovies.fragments.listfragments.ListPopularFragment;
import io.magics.popularmovies.fragments.listfragments.ListTopRatedFragment;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.networkutils.DataProvider;
import io.magics.popularmovies.utils.MovieUtils.ScrollDirection;
import io.magics.popularmovies.viewmodels.FavListViewModel;
import io.magics.popularmovies.viewmodels.PopListViewModel;
import io.magics.popularmovies.viewmodels.ReviewsViewModel;
import io.magics.popularmovies.viewmodels.TopListViewModel;
import io.magics.popularmovies.viewmodels.TrailersViewModel;

public class MovieListsActivity extends AppCompatActivity implements ListTopRatedFragment.TopRatedFragmentListener,
        ListPopularFragment.PopularFragmentListener, ListFavouritesFragment.FavouritesFragmentListener,
        MovieDetailsFragment.DetailFragInteractionHandler {

    //TODO Add horizontal layout support.
    //TODO Inspect for memoryleaks

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

    private static final String DETAIL_FRAGMENT_TAG = "detailFrag";

    MovieListsPagerAdapter mAdapter;
    DataProvider mDataProvider;

    TopListViewModel mTopListVM;
    PopListViewModel mPopListVM;
    FavListViewModel mFavListVM;
    TrailersViewModel mTrailerVm;
    ReviewsViewModel mReviewVm;

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

        mViewPager.setOffscreenPageLimit(3);
        mAdapter = new MovieListsPagerAdapter(getSupportFragmentManager());

        mAppBar.setOrientation(AppBarLayout.VERTICAL);

        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING && mUpFab.getVisibility() == View.VISIBLE) mUpFab.hide();
                else if (state == ViewPager.SCROLL_STATE_IDLE && mUpFab.getVisibility() != View.VISIBLE) mUpFab.show();
                super.onPageScrollStateChanged(state);
            }
        });

        setClickListenerOnUpFab();

        mTabLayout.setupWithViewPager(mViewPager);

        mDataProvider = new DataProvider(this, mTopListVM, mPopListVM, mFavListVM,
                mTrailerVm, mReviewVm);

        mDataProvider.initialiseApp();

    }

    @Override
    protected void onDestroy() {
        if (mDataProvider != null) mDataProvider.dispose();
        if (mUnbinder != null) mUnbinder.unbind();
        super.onDestroy();
    }


    public void showMovieDetailsFrag(Movie movie) {

        mDataProvider.setMovieAndFetch(movie);

        mUpFab.hide();
        mAppBarBack.setMinimumHeight(mTabLayout.getHeight());
        mAppBar.setBackgroundResource(R.drawable.bg_toolbar_list);
        mTabLayout.setVisibility(View.GONE);
        mAppBarBack.setVisibility(View.VISIBLE);
        mAppBarBack.setOnClickListener(v -> {
            Fragment frag = getSupportFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (frag != null){
                ft.remove(frag);
                ft.commit();
            }
        });

        MovieDetailsFragment frag = MovieDetailsFragment.newInstance(movie, mFavListVM.checkIfFavourite(movie.getMovieId()));
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container_main, frag, DETAIL_FRAGMENT_TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void hideUpFabOnScroll(ScrollDirection scrollDirection){
        if (scrollDirection == ScrollDirection.SCROLL_DOWN && mUpFab.getVisibility() == View.VISIBLE) mUpFab.hide();
        else if (scrollDirection == ScrollDirection.SCROLL_UP && mUpFab.getVisibility() != View.VISIBLE) mUpFab.show();
    }

    public void setClickListenerOnUpFab(){
        mUpFab.setOnClickListener(v -> {
            //Using try catch block instead of checking if fragment is null
            try {
                switch (mTabLayout.getSelectedTabPosition()) {
                    case 0:
                        mAdapter.getTopFrag().scrollTopListToZero();
                        break;
                    case 1:
                        mAdapter.getPopFrag().scrollPopListToZero();
                        break;
                    case 2:
                        mAdapter.getFavFrag().scrollFavListToZero();
                        break;
                    default:
                        break;
                }
            } catch (NullPointerException e){
                //Does nothing. Fragment not instantiated yet.
            }
        });
    }

    @Override
    public void showClickedTopMovie(Movie movie) {
        showMovieDetailsFrag(movie);
    }

    @Override
    public void topRatedRvScrolled(ScrollDirection scrollDirection) {
        hideUpFabOnScroll(scrollDirection);
    }

    @Override
    public void showClickedPopMovie(Movie movie) {
        showMovieDetailsFrag(movie);
    }

    @Override
    public void popularRvScrolled(ScrollDirection scrollDirection) {
        hideUpFabOnScroll(scrollDirection);
    }

    @Override
    public void showClickedFavMovie(Movie movie) {
        showMovieDetailsFrag(movie);
    }

    @Override
    public void favouriteRvScrolled(ScrollDirection scrollDirection) {
        hideUpFabOnScroll(scrollDirection);
    }

    @Override
    public void favFabClicked(Movie movie, Boolean isFavourite) {
        if (isFavourite) mDataProvider.deleteFromFavourites(movie);
        else mDataProvider.addToFavourites(movie);
    }

    @Override
    public void onFragmentExit() {
        if (!isChangingConfigurations() || !isFinishing() && mUpFab != null) {
            mUpFab.show();
            mTabLayout.setVisibility(View.VISIBLE);
            mAppBarBack.setVisibility(View.GONE);
            mAppBar.setBackground(null);
        }
    }
}
