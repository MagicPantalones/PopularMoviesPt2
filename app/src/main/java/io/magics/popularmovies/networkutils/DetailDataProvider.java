package io.magics.popularmovies.networkutils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import io.magics.popularmovies.BuildConfig;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.models.ReviewResult;
import io.magics.popularmovies.models.Reviews;
import io.magics.popularmovies.models.Trailers;
import io.magics.popularmovies.networkutils.TMDBApi;
import io.magics.popularmovies.utils.MovieUtils;
import io.magics.popularmovies.viewmodels.ReviewsViewModel;
import io.magics.popularmovies.viewmodels.TopListViewModel;
import io.magics.popularmovies.viewmodels.TrailersViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.magics.popularmovies.utils.MovieUtils.getClientForMovieList;

public class DetailDataProvider {

    private static final String TMDB_API_KEY = BuildConfig.TMDB_API_KEY;
    private static final String LOCALE = "en-US";

    private final TrailersViewModel mTrailerVM;
    private final ReviewsViewModel mReviewsVM;

    private final Movie mMovie;
    private Disposable mTAndRDisposable;
    private Disposable mMoreReviewsDisposable;

    private List<ReviewResult> mReviewsToViewModel = new ArrayList<>();

    public DetailDataProvider(TrailersViewModel trailerVm, ReviewsViewModel reviewVm, Movie movie){
        mTrailerVM = trailerVm;
        mReviewsVM = reviewVm;
        mMovie = movie;
    }

    public void initDetailFragment(){
        mTAndRDisposable = getTrailersAndReviews();
    }

    public void disposeDetailsProvider(){
        if (mTAndRDisposable != null && mTAndRDisposable.isDisposed()) mTAndRDisposable.dispose();
        if (mMoreReviewsDisposable != null && mMoreReviewsDisposable.isDisposed()) mMoreReviewsDisposable.dispose();
    }

    private Disposable getTrailersAndReviews(){
        return getClientForMovieList().create(TMDBApi.class)
                .getTrailersAndReviews(mMovie.getMovieId(), TMDB_API_KEY, LOCALE, "videos,reviews")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(trailersAndReviews -> {

                    Reviews reviews = trailersAndReviews.getReviews();
                    Trailers trailers = trailersAndReviews.getTrailers();

                    mReviewsToViewModel.addAll(reviews.getReviewResults());
                    mTrailerVM.setTrailers(trailers.getTrailerResults());

                    if (reviews.getTotalPages() > 1){
                        mMoreReviewsDisposable = getMoreReviewPages(2);
                    } else {
                        mReviewsVM.setReviews(mReviewsToViewModel);
                    }

                });
    }

    private Disposable getMoreReviewPages(int page){
        return getClientForMovieList().create(TMDBApi.class)
                .getMoreReviews(mMovie.getMovieId(), TMDB_API_KEY, LOCALE, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(reviews -> {
                    mReviewsToViewModel.addAll(reviews.getReviewResults());
                    mReviewsVM.setReviews(mReviewsToViewModel);
                });
    }

}
