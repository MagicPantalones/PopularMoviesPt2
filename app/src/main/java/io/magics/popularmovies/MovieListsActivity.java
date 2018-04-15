package io.magics.popularmovies;

import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.stetho.Stetho;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.fragments.detailfragments.MovieDetailsFragment;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.networkutils.ListDataProvider;
import io.magics.popularmovies.viewmodels.FavListViewModel;
import io.magics.popularmovies.viewmodels.PopListViewModel;
import io.magics.popularmovies.viewmodels.ReviewsViewModel;
import io.magics.popularmovies.viewmodels.TopListViewModel;
import io.magics.popularmovies.viewmodels.TrailersViewModel;

import static io.magics.popularmovies.utils.MovieUtils.isConnected;

public class MovieListsActivity extends AppCompatActivity {

    //TODO Lifecycle Persistance
    //TODO Add horizontal layout support.
    //TODO Inspect for memoryleaks

    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.sliding_tabs)
    TabLayout mTabLayout;
    @BindView(R.id.up_fab)
    FloatingActionButton mUpFab;

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
        boolean favCheck = false;

        MovieDetailsFragment frag = MovieDetailsFragment.newInstance(movie, mFavListVM.checkIfFavourite(movie.getMovieId()));
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.container_main, frag);
        ft.addToBackStack(null);
        ft.commit();
    }

}
