package io.magics.popularmovies.networkutils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.magics.popularmovies.BuildConfig;
import io.magics.popularmovies.database.PopularMoviesDBHelper;
import io.magics.popularmovies.database.PopularMoviesDBHelper.MovieEntries;
import io.magics.popularmovies.models.ApiResult;
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
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.schedulers.Schedulers;

import static io.magics.popularmovies.utils.MovieUtils.createMovieFromCursor;
import static io.magics.popularmovies.utils.MovieUtils.getClientForMovieList;
import static io.magics.popularmovies.utils.MovieUtils.makeContentVals;

/**
 * <p>This data class does all calls, except for downloading movie posters (Glide does that), for data
 * from TheMovieDataBase API.</p>
 *
 * <p>I decided of going for a seperate class to handle all of the networking and async tasks, like
 * reading and writing to the database. And doing all calls and SQLite fetching using RxJava
 * together with a Retrofit & OkHttp3 client.</p>
 *
 * <p>{@link #initialiseApp()} Does all the initial calls, as well as registering this instance as
 * {@link #getTopPages()} & {@link #getPopPages()} listeners. These listeners just gets the next
 * API page if available. <br> The movie lists that are returned from TMDB is usually to long to reach
 * the end manually, but I still made the check as a routine</p>
 *
 * <p>The {@link #setMovieAndFetch(Movie)} method is called only when a movie is selected in the list
 * and will do a call for the movie details from TMDB.</p>
 *
 * <p>Since reading and writing to the SQLite DB needs a Context, an activity context,
 * {@link #mContext}, is passed in the constructor. <br>
 * I have not found any memory leaks yet with LeakCanary, and I have not noticed any yet either.
 * </p>
 *
 * <p>NOTE! {@link #TMDB_API_KEY} is set from the BuildConfig file. Create a variable called
 * {@code TheMovieDbApiKey}, in the {@code gradle.properties} file, and assign your API key to it.</p>
 *
 */
public class DataProvider
        implements TopListViewModel.GetMoreTopPagesListener, PopListViewModel.GetMorePopPagesListener{

    private static final String TAG = DataProvider.class.getSimpleName();

    private static final String[] MOVIE_DB_COLUMNS = {
            MovieEntries._ID,
            MovieEntries.COLUMN_POSTER_PATH,
            MovieEntries.COLUMN_OVERVIEW,
            MovieEntries.COLUMN_RELEASE_DATE,
            MovieEntries.COLUMN_MOVIE_ID,
            MovieEntries.COLUMN_TITLE,
            MovieEntries.COLUMN_VOTE_AVERAGE,
            MovieEntries.COLUMN_COLOR_PATH
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

    private Disposable mDbInsertTopDisposable;
    private Disposable mDbInsertPopDisposable;
    private Disposable mCachedTopQueryDisposable;
    private Disposable mCachedPopQueryDisposable;
    private Disposable mDeleteTopTableDisposable;
    private Disposable mDeletePopTableDisposable;

    private final TopListViewModel mTopVm;
    private final PopListViewModel mPopVm;
    private final FavListViewModel mFavVm;
    private final TrailersViewModel mTrailerVm;
    private final ReviewsViewModel mReviewVm;

    private final PopularMoviesDBHelper mDbHelper;

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
        this.mDbHelper = new PopularMoviesDBHelper(context);
    }

    public DataProvider(Context context, TopListViewModel topVm, PopListViewModel popVm,
                        FavListViewModel favVm, TrailersViewModel trailerVm,
                        ReviewsViewModel reviewVm, List<ApiResult> vmState){
        this.mContext = context;
        this.mTopVm = topVm;
        this.mPopVm = popVm;
        this.mFavVm = favVm;
        this.mTrailerVm = trailerVm;
        this.mReviewVm = reviewVm;
        this.mDbHelper = new PopularMoviesDBHelper(context);

        mTopVm.setPages(vmState.get(0));
        mPopVm.setPages(vmState.get(1));
    }

    public void initialiseApp(){
        mTopVm.addGetMoreTopPagesListener(this);
        mPopVm.addGetMorePopPagesListener(this);
        mCachedTopQueryDisposable = queryForCachedMovieLists(MovieEntries.TABLE_TOP_RATED);
        mCachedPopQueryDisposable = queryForCachedMovieLists(MovieEntries.TABLE_POPULAR);
        mFavDisposable = getFavList();
    }

    public void dispose() {
        disposeAllDisposables();
        unregisterListeners();
    }

    private void unregisterListeners() {
        mTopVm.unregisterTopPagesListener();
        mPopVm.unregisterPopPagesListener();
    }

    private void disposeAllDisposables() {
        disposeDisposable(mTopDisposable,
                mPopDisposable,
                mFavDisposable,
                mTAndRDisposable,
                mMoreReviewsDisposable,
                mCachedTopQueryDisposable,
                mCachedPopQueryDisposable,
                mDbInsertTopDisposable,
                mDbInsertPopDisposable,
                mDeleteTopTableDisposable,
                mDeletePopTableDisposable);
    }

    private void disposeDisposable(Disposable... disposables) {
        for (Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) disposable.dispose();
        }
    }

    public void setMovieAndFetch(Movie movie){
        mTrailerVm.clear();
        mReviewVm.clear();
        mDetailMovie = movie;
        mTAndRDisposable = getTrailersAndReviews();
    }

    public void debugDb(){
        disposeAllDisposables();
        mDbHelper.deleteAllFromTable(MovieEntries.TABLE_TOP_RATED);
        mDbHelper.deleteAllFromTable(MovieEntries.TABLE_POPULAR);
    }

    public void refreshList(int listType){
        switch (listType) {
            case 0:
                disposeDisposable(mTopDisposable,
                        mCachedTopQueryDisposable,
                        mDbInsertTopDisposable,
                        mDeleteTopTableDisposable);
                mTopVm.clearPages();
                mDeleteTopTableDisposable = deleteTableAndRefresh(MovieEntries.TABLE_TOP_RATED);
                break;
            case 1:
                disposeDisposable(mPopDisposable,
                        mCachedPopQueryDisposable,
                        mDbInsertPopDisposable,
                        mDeletePopTableDisposable);
                mTopVm.clearPages();
                mDeletePopTableDisposable = deleteTableAndRefresh(MovieEntries.TABLE_POPULAR);
                break;
            case 3:
                disposeDisposable(mFavDisposable);
                mFavVm.clearPages();
                mFavDisposable = getFavList();
                break;
            default:
                //Should never happen
        }
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
                    mDbInsertTopDisposable = batchInsertMovies(callback.getMovies(),
                            MovieEntries.TABLE_TOP_RATED);
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
                    mDbInsertPopDisposable = batchInsertMovies(callback.getMovies(),
                            MovieEntries.TABLE_POPULAR);
                    mPopVm.setPages(callback);
                    mPopVm.setPopList(callback.getMovies());
                }, throwable -> mPopVm.setPopList(new ArrayList<>()));
    }

    private Disposable batchInsertMovies(List<Movie> movies, String tableName) {
        return Observable.just(mDbHelper.batchInsertMovies(movies, tableName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (aLong == -1) {
                        throw new UndeliverableException(new Throwable());
                    }
                    Log.w(TAG, "Inserted " + aLong + " new rows to " + tableName);
                }, throwable -> Log.e(TAG, "Error inserting api call to DB.", throwable));
    }

    private Disposable deleteTableAndRefresh(String tableName) {
        return Observable.just(mDbHelper.deleteAllFromTable(tableName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    Log.w(TAG, "Deleted " + integer + " rows from " + tableName);
                    if (tableName.equals(MovieEntries.TABLE_TOP_RATED)) getTopList(mTopVm.getCurrentPage());
                    else getPopList(mPopVm.getCurrentPage());
                });
    }

    private Disposable queryForCachedMovieLists(final String tableName) {

        return Observable.just(mDbHelper.getAllMovies(tableName, MOVIE_DB_COLUMNS))
                .subscribeOn(Schedulers.io())
                .map(this::processCursor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movies -> {
                    if (!movies.isEmpty()){
                        if (tableName.equals(MovieEntries.TABLE_TOP_RATED)) {
                            mTopVm.setTopList(movies);
                        } else {
                            mPopVm.setPopList(movies);
                        }
                    } else {
                        throw new UndeliverableException(
                                new Throwable("Movie List From DB empty: " + movies.size()));
                    }
                }, throwable -> {
                    Log.w(TAG, "Could not restore SavedInstanceState, getting page 1.",
                            throwable);
                    if (tableName.equals(MovieEntries.TABLE_TOP_RATED)) {
                        mTopVm.clearPages();
                        mTopDisposable = getTopList(mTopVm.getCurrentPage());
                    } else {
                        mPopVm.clearPages();
                        mPopDisposable = getPopList(mPopVm.getCurrentPage());
                    }
                });

    }

    @SuppressWarnings("ConstantConditions")
    private Disposable getFavList(){
        ContentResolver cr = mContext.getContentResolver();
        String sortOrder = MovieEntries._ID + " ASC";

        return Observable.just(cr.query(MovieEntries.FAVOURITES_CONTENT_URI,
                MOVIE_DB_COLUMNS,
                null,
                null,
                sortOrder))
                .subscribeOn(Schedulers.io())
                .map(this::processCursor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mFavVm::setFavList);
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("CheckResult")
    public void addToFavourites(Movie movie){
        ContentResolver cr = mContext.getContentResolver();
        Observable.just(cr.insert(MovieEntries.FAVOURITES_CONTENT_URI, makeContentVals(movie)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uri -> mFavVm.addToList(movie), throwable -> Log.d("DataProvider", "addToFavourites: " + throwable.getMessage()));
    }

    @SuppressLint("CheckResult")
    public void deleteFromFavourites(Movie movie){
        ContentResolver cr = mContext.getContentResolver();
        Observable.just(cr.delete(
                MovieEntries.FAVOURITES_CONTENT_URI,
                MovieEntries.COLUMN_MOVIE_ID + "= " + Integer.toString(movie.getMovieId()),
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

                    mReviewsToViewModel = reviews.getReviewResults();
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
    /*
    As the API rarely returns more than one page of reviews. The class only calls for a second
    page if there are more than 1.
    Will make a similar listener for the reviews if the API gets more popular and the movies gets
    more reviews.
    */
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

    private List<Movie> processCursor(Cursor cursor) {
        List<Movie> moviesFromCursor = new ArrayList<>();
        if (cursor != null){
            while (cursor.moveToNext()) {
                moviesFromCursor.add(createMovieFromCursor(cursor, cursor.getPosition()));
            }
            cursor.close();
        }
        return moviesFromCursor;
    }
}
