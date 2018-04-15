package io.magics.popularmovies;

import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

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
import io.magics.popularmovies.networkutils.ListDataProvider;
import io.magics.popularmovies.utils.MovieUtils;
import io.magics.popularmovies.viewmodels.FavListViewModel;
import io.magics.popularmovies.viewmodels.PopListViewModel;
import io.magics.popularmovies.viewmodels.ReviewsViewModel;
import io.magics.popularmovies.viewmodels.TopListViewModel;
import io.magics.popularmovies.viewmodels.TrailersViewModel;

import static io.magics.popularmovies.utils.MovieUtils.isConnected;

public class MovieListsActivity extends AppCompatActivity implements ListTopRatedFragment.TopRatedFragmentListener,
        ListPopularFragment.PopularFragmentListener, ListFavouritesFragment.FavouritesFragmentListener{

    //TODO Lifecycle Persistance
    //TODO Add horizontal layout support.
    //TODO Inspect for memoryleaks

    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.sliding_tabs)
    TabLayout mTabLayout;
    @BindView(R.id.up_fab)
    FloatingActionButton mUpFab;

    MovieListsPagerAdapter mAdapter;
    ListDataProvider mDataProvider;

    TopListViewModel mTopListVM;
    PopListViewModel mPopListVM;
    FavListViewModel mFavListVM;

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

        setClickListenerOnUpFab();

        mTabLayout.setupWithViewPager(mViewPager);

        mDataProvider = new ListDataProvider(this, mTopListVM, mPopListVM, mFavListVM);

        mDataProvider.initialiseApp();

    }

    @Override
    protected void onDestroy() {
        if (mDataProvider != null) mDataProvider.dispose();
        if (mUnbinder != null) mUnbinder.unbind();
        super.onDestroy();
    }


    public void showMovieDetailsFrag(Movie movie) {

        MovieDetailsFragment frag = MovieDetailsFragment.newInstance(movie, mFavListVM.checkIfFavourite(movie.getMovieId()));
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.container_main, frag);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void hideUpFabOnScroll(MovieUtils.ScrollDirection scrollDirection){
        if (scrollDirection == MovieUtils.ScrollDirection.SCROLL_DOWN && mUpFab.getVisibility() == View.VISIBLE) mUpFab.hide();
        else if (scrollDirection == MovieUtils.ScrollDirection.SCROLL_UP && mUpFab.getVisibility() != View.VISIBLE) mUpFab.show();
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
    public void topRatedRvScrolled(MovieUtils.ScrollDirection scrollDirection) {
        hideUpFabOnScroll(scrollDirection);
    }

    @Override
    public void showClickedPopMovie(Movie movie) {
        showMovieDetailsFrag(movie);
    }

    @Override
    public void popularRvScrolled(MovieUtils.ScrollDirection scrollDirection) {
        hideUpFabOnScroll(scrollDirection);
    }

    @Override
    public void showClickedFavMovie(Movie movie) {
        showMovieDetailsFrag(movie);
    }

    @Override
    public void favouriteRvScrolled(MovieUtils.ScrollDirection scrollDirection) {
        hideUpFabOnScroll(scrollDirection);
    }

}
