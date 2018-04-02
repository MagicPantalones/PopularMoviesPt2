package io.magics.popularmovies;

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
import io.magics.popularmovies.networkutils.TMDBApi.SortingMethod;
import io.magics.popularmovies.viewmodels.FavListViewModel;
import io.magics.popularmovies.viewmodels.PopListViewModel;
import io.magics.popularmovies.viewmodels.ReviewsViewModel;
import io.magics.popularmovies.viewmodels.TopListViewModel;
import io.magics.popularmovies.viewmodels.TrailersViewModel;
import io.reactivex.disposables.Disposable;

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

    TopListViewModel mTopListViewModel;
    PopListViewModel mPopListViewModel;
    FavListViewModel mFavListViewModel;
    TrailersViewModel mTrailersViewModel;
    ReviewsViewModel mReviewsViewModel;

    Disposable mTopDisposable;
    Disposable mPopDisposable;
    Disposable mFavDisposable;
    Disposable mTAndRDisposable;
    Disposable mMoreRevDisposable;

    Unbinder mUnbinder;

    boolean mConnected = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_lists);
        mUnbinder = ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);

        getInitFavouritesList();

        mConnected = isConnected(this);
        if (mConnected) {
            getTopRatedList();
            getPopularList();
        }
    }

    @Override
    protected void onDestroy() {
        if (mTopDisposable != null && !mTopDisposable.isDisposed()) mTopDisposable.dispose();
        if (mPopDisposable != null && !mPopDisposable.isDisposed()) mPopDisposable.dispose();
        if (mFavDisposable != null && !mFavDisposable.isDisposed()) mFavDisposable.dispose();
        if (mTAndRDisposable != null && !mTAndRDisposable.isDisposed()) mTAndRDisposable.dispose();
        if (mMoreRevDisposable != null && !mMoreRevDisposable.isDisposed()) mMoreRevDisposable.dispose();
        if (mUnbinder != null) mUnbinder.unbind();
        super.onDestroy();
    }

    public boolean hasRequestedFromNetwork(){
        return mConnected;
    }

    private void getTopRatedList() {
        if (mTopListViewModel.isLastPageSet() && mTopListViewModel.isLastPageLoaded()) return;
        mTopDisposable = ApiUtils.callApiForMovieList(SortingMethod.TOP_RATED, mTopListViewModel.getCurrentPage(),
                apiResult -> {
                    mTopListViewModel.setTopList(apiResult.getMovies());
                    mTopListViewModel.setPages(apiResult);
                });
    }

    private void getPopularList() {
        if (mPopLastPage != -1 && mPopPageNum >= mPopLastPage) return;
        mPopDisposable = ApiUtils.callApiForMovieList(SortingMethod.POPULAR, mPopPageNum,
                apiResult -> {
                    mPopMovieList.addAll(apiResult.getMovies());
                    mPopPageNum = apiResult.getPage();
                    if (mPopLastPage == -1) mPopLastPage = apiResult.getTotalPages();

                });
    }

    private void getInitFavouritesList() {
        mFavDisposable = ThreadingUtils.queryForFavouriteMovies(this,
                movies -> {
                    mFavMovieList = movies;
                });
    }

    private void getTrailersAndReviews(int movieId) {
        mTAndRDisposable = ApiUtils.callApiForTrailersAndReviews(movieId,
                results -> {
                    mReviews.addAll(results.getReviews().getReviewResults());
                    mTrailers.addAll(results.getTrailers().getTrailerResults());
                    mReviewPageNum = results.getReviews().getPage();

                    if (mReviewLastPage == -1) mReviewLastPage = results.getReviews().getTotalPages();

                });
    }

    private void getMoreReviews(int movieId) {
        mReviewPageNum += 1;
        if (mReviewLastPage != -1 && mReviewPageNum >= mReviewLastPage) return;
        mMoreRevDisposable = ApiUtils.callForMoreReviews(movieId, mReviewPageNum,
                result -> {
                    mReviews.addAll(result.getReviewResults());
                    mReviewPageNum = result.getPage();
                });
    }

    public void addToFavourites(Movie movie) {
        ThreadingUtils.addToFavourites(this, movie, (success, favUri) -> {
            if (success) {
                mFavMovieList.add(movie);
            }
        });
    }

    public void deleteFromFavourites(Movie movie) {
        ThreadingUtils.deleteFromFavourites(this, movie, success -> {
            if (success >= 1) {
                mFavMovieList.remove(movie);
            }
        });
    }

    public void showMovieDetailsFrag(Movie movie) {
        boolean favCheck = false;
        getTrailersAndReviews(movie.getMovieId());

        for (Movie m : mFavMovieList){
            if (m.getMovieId().equals(movie.getMovieId())){
                favCheck = true;
                break;
            }
        }

        MovieDetailsFragment frag = MovieDetailsFragment.newInstance(movie, favCheck);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.container_main, frag);
        ft.addToBackStack(null);
        ft.commit();
    }

}
