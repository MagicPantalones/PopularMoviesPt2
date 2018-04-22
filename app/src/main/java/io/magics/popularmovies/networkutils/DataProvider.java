package io.magics.popularmovies.networkutils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.magics.popularmovies.BuildConfig;
import io.magics.popularmovies.database.FavouritesDBHelper.FavouritesEntry;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.models.ReviewResult;
import io.magics.popularmovies.models.Reviews;
import io.magics.popularmovies.models.Trailers;
import io.magics.popularmovies.viewmodels.FavListViewModel;
import io.magics.popularmovies.viewmodels.PopListViewModel;
import io.magics.popularmovies.viewmodels.ReviewsViewModel;
import io.magics.popularmovies.viewmodels.TopListViewModel;
import io.magics.popularmovies.viewmodels.TrailersViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static io.magics.popularmovies.utils.MovieUtils.createMovieFromCursor;
import static io.magics.popularmovies.utils.MovieUtils.getClientForMovieList;
import static io.magics.popularmovies.utils.MovieUtils.makeContentVals;

public class DataProvider
        implements TopListViewModel.GetMoreTopPagesListener, PopListViewModel.GetMorePopPagesListener{

    private static final String[] FAVOURITES_COLUMNS = {
            FavouritesEntry._ID,
            FavouritesEntry.COLUMN_POSTER_PATH,
            FavouritesEntry.COLUMN_OVERVIEW,
            FavouritesEntry.COLUMN_RELEASE_DATE,
            FavouritesEntry.COLUMN_MOVIE_ID,
            FavouritesEntry.COLUMN_TITLE,
            FavouritesEntry.COLUMN_VOTE_AVERAGE,
            FavouritesEntry.COLUMN_COLOR_PATH
    };


    private static final String SORT_TOP = "top_rated";
    private static final String SORT_POP = "popular";
    private static final String TMDB_API_KEY = BuildConfig.TMDB_API_KEY;
    private static final String LOCALE = "en-US";

    private final Context mContext;
    private Disposable mTopDisposable;
    private Disposable mPopDisposable;
    private Disposable mFavDisposable;

    private Disposable mTAndRDisposable;
    private Disposable mMoreReviewsDisposable;

    private final TopListViewModel mTopVm;
    private final PopListViewModel mPopVm;
    private final FavListViewModel mFavVm;
    private final TrailersViewModel mTrailerVm;
    private final ReviewsViewModel mReviewVm;

    private Movie mDetailMovie;
    private List<ReviewResult> mReviewsToViewModel = new ArrayList<>();


    public DataProvider(Context context, TopListViewModel topVm, PopListViewModel popVm,
                        FavListViewModel favVm, TrailersViewModel trailerVm, ReviewsViewModel reviewVm){
        this.mContext = context;
        this.mTopVm = topVm;
        this.mPopVm = popVm;
        this.mFavVm = favVm;
        this.mTrailerVm = trailerVm;
        this.mReviewVm = reviewVm;
    }

    public void initialiseApp(){
        mTopVm.addGetMoreTopPagesListener(this);
        mPopVm.addGetMorePopPagesListener(this);
        mTopDisposable = getTopList(mTopVm.getCurrentPage());
        mPopDisposable = getPopList(mPopVm.getCurrentPage());
        mFavDisposable = getFavList();
    }

    public void dispose() {
        if (mTopDisposable != null && !mTopDisposable.isDisposed()) mTopDisposable.dispose();
        if (mPopDisposable != null && !mPopDisposable.isDisposed()) mPopDisposable.dispose();
        if (mFavDisposable != null && !mFavDisposable.isDisposed()) mFavDisposable.dispose();
        if (mTAndRDisposable != null && mTAndRDisposable.isDisposed()) mTAndRDisposable.dispose();
        if (mMoreReviewsDisposable != null && mMoreReviewsDisposable.isDisposed()) mMoreReviewsDisposable.dispose();
        mTopVm.unregisterTopPagesListener();
        mPopVm.unregisterPopPagesListener();
    }

    public void setMovieAndFetch(Movie movie){
        mTrailerVm.clear();
        mReviewVm.clear();
        mDetailMovie = movie;
        mTAndRDisposable = getTrailersAndReviews();
    }

    @Override
    public void getTopPages() {
        if (mTopVm.isLastPageLoaded()) return;
        mTopDisposable = getTopList(mTopVm.getCurrentPage() + 1);
    }

    @Override
    public void getPopPages() {
        if (mPopVm.isLastPageLoaded()) return;
        mPopDisposable = getPopList(mPopVm.getCurrentPage() + 1);
    }

    private Disposable getTopList(int pageNumber){
        return getClientForMovieList().create(TMDBApi.class)
                .getMovieList(SORT_TOP, TMDB_API_KEY, LOCALE, pageNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback -> {
                    mTopVm.setPages(callback);
                    mTopVm.setTopList(callback.getMovies());
                }, throwable -> mTopVm.setTopList(new ArrayList<>()));
    }

    private Disposable getPopList(int pageNumber){
        return getClientForMovieList().create(TMDBApi.class)
                .getMovieList(SORT_POP, TMDB_API_KEY, LOCALE, pageNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback -> {
                    mPopVm.setPages(callback);
                    mPopVm.setPopList(callback.getMovies());
                }, throwable -> mPopVm.setPopList(new ArrayList<>()));
    }

    @SuppressWarnings("ConstantConditions")
    private Disposable getFavList(){
        ContentResolver cr = mContext.getContentResolver();
        String sortOrder = FavouritesEntry._ID + " ASC";

        return Observable.just(cr.query(FavouritesEntry.FAVOURITES_CONTENT_URI,
                FAVOURITES_COLUMNS,
                null,
                null,
                sortOrder))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(cursor -> {
                    if (cursor != null) {
                        List<Movie> moviesFromCursor = new ArrayList<>(cursor.getCount());
                        while (cursor.moveToNext()) {
                            moviesFromCursor.add(createMovieFromCursor(cursor, cursor.getPosition()));
                        }
                        cursor.close();
                        return moviesFromCursor;
                    }
                    return new ArrayList<Movie>();
                })
                .subscribe(mFavVm::setFavList);
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("CheckResult")
    public void addToFavourites(Movie movie){
        ContentResolver cr = mContext.getContentResolver();
        Observable.just(cr.insert(FavouritesEntry.FAVOURITES_CONTENT_URI, makeContentVals(movie)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uri -> mFavVm.addToList(movie), throwable -> Log.d("DataProvider", "addToFavourites: " + throwable.getMessage()));
    }

    @SuppressLint("CheckResult")
    public void deleteFromFavourites(Movie movie){
        ContentResolver cr = mContext.getContentResolver();
        Observable.just(cr.delete(
                FavouritesEntry.FAVOURITES_CONTENT_URI,
                FavouritesEntry.COLUMN_MOVIE_ID + "= " + Integer.toString(movie.getMovieId()),
                null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> mFavVm.removeFromList(movie),
                        throwable -> Log.d("DataProvider", "deleteFromFavourites: " + throwable.getMessage()));
    }



    private Disposable getTrailersAndReviews(){
        return getClientForMovieList().create(TMDBApi.class)
                .getTrailersAndReviews(mDetailMovie.getMovieId(), TMDB_API_KEY, LOCALE, "videos,reviews")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(trailersAndReviews -> {

                    Reviews reviews = trailersAndReviews.getReviews();
                    Trailers trailers = trailersAndReviews.getTrailers();

                    mReviewsToViewModel.addAll(reviews.getReviewResults());
                    mTrailerVm.setTrailers(trailers.getTrailerResults());

                    if (reviews.getTotalPages() > 1){
                        mMoreReviewsDisposable = getMoreReviewPages();
                    } else {
                        mReviewVm.setReviews(mReviewsToViewModel);
                    }

                }, throwable -> {
                    mReviewVm.setReviews(new ArrayList<>());
                    mTrailerVm.setTrailers(new ArrayList<>());
                });
    }

    private Disposable getMoreReviewPages(){
        return getClientForMovieList().create(TMDBApi.class)
                .getMoreReviews(mDetailMovie.getMovieId(), TMDB_API_KEY, LOCALE, 2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(reviews -> {
                    mReviewsToViewModel.addAll(reviews.getReviewResults());
                    mReviewVm.setReviews(mReviewsToViewModel);
                }, throwable -> mReviewVm.setReviews(mReviewsToViewModel));
    }


}
