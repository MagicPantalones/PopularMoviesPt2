package io.magics.popularmovies.networkutils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.magics.popularmovies.utils.MovieUtils.createMovieFromCursor;
import static io.magics.popularmovies.utils.MovieUtils.makeContentVals;
import static io.magics.popularmovies.utils.MovieUtils.setMoviePageNumbers;

/**
 * <p>This data class does all calls, except for downloading movie posters (Glide does that), for data
 * from TheMovieDataBase API.</p>
 * <p>
 * <p>I decided of going for a seperate class to handle all of the networking and async tasks, like
 * reading and writing to the database. And doing all calls and SQLite fetching using RxJava
 * together with a Retrofit & OkHttp3 client.</p>
 * <p>
 * <p>{@link #initialiseApp()} Does all the initial calls, as well as registering this instance as
 * {@link #getTopPages()} & {@link #getPopPages()} listeners. These listeners just gets the next
 * API page if available. <br> The movie lists that are returned from TMDB is usually to long to reach
 * the end manually, but I still made the check as a routine</p>
 * <p>
 * <p>The {@link #setMovieAndFetch(Movie)} method is called only when a movie is selected in the list
 * and will do a call for the movie details from TMDB.</p>
 * <p>
 * <p>Since reading and writing to the SQLite DB needs a Context, an activity context,
 * {@link #mContext}, is passed in the constructor. <br>
 * I have not found any memory leaks yet with LeakCanary, and I have not noticed any yet either.
 * </p>
 * <p>
 * <p>NOTE! {@link #TMDB_API_KEY} is set from the BuildConfig file. Create a variable called
 * {@code TheMovieDbApiKey}, in the {@code gradle.properties} file, and assign your API key to it.</p>
 */
public class DataProvider
        implements TopListViewModel.GetMoreTopPagesListener, PopListViewModel.GetMorePopPagesListener {

    private static final String[] MOVIE_DB_COLUMNS = {
            MovieEntries._ID,
            MovieEntries.COLUMN_POSTER_PATH,
            MovieEntries.COLUMN_OVERVIEW,
            MovieEntries.COLUMN_RELEASE_DATE,
            MovieEntries.COLUMN_MOVIE_ID,
            MovieEntries.COLUMN_TITLE,
            MovieEntries.COLUMN_VOTE_AVERAGE,
            MovieEntries.COLUMN_COLOR_PATH,
            MovieEntries.COLUMN_PAGE_NUMBER
    };


    private static final String SORT_TOP = "top_rated";
    private static final String SORT_POP = "popular";
    private static final String TMDB_API_KEY = BuildConfig.TMDB_API_KEY;
    private static final String LOCALE = "en-US";
    private static final int CONNECTION_TIMEOUT = 10;
    private static final String BASE_QUERY_API_URL = "https://api.themoviedb.org/3/";

    private final Context mContext;
    private Disposable mTopDisposable;
    private Disposable mPopDisposable;
    private Disposable mFavDisposable;

    private Disposable mTAndRDisposable;

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
    private List<Movie> mTopMoviesToDb = new ArrayList<>();
    private List<Movie> mPopMoviesToDb = new ArrayList<>();

    private boolean mIsOnline;

    private InternetConnectionListener mConnectionListener;

    public DataProvider(Context context, TopListViewModel topVm, PopListViewModel popVm,
                        FavListViewModel favVm, TrailersViewModel trailerVm,
                        ReviewsViewModel reviewVm, InternetConnectionListener listener) {
        this.mContext = context;
        this.mTopVm = topVm;
        this.mPopVm = popVm;
        this.mFavVm = favVm;
        this.mTrailerVm = trailerVm;
        this.mReviewVm = reviewVm;
        this.mDbHelper = new PopularMoviesDBHelper(context);
        this.mConnectionListener = listener;
    }

    public void initialiseApp() {
        mTopVm.addGetMoreTopPagesListener(this);
        mPopVm.addGetMorePopPagesListener(this);
        mCachedTopQueryDisposable = queryForCachedMovieLists(MovieEntries.TABLE_TOP_RATED);
        mCachedPopQueryDisposable = queryForCachedMovieLists(MovieEntries.TABLE_POPULAR);
        mFavDisposable = getFavList();
    }

    public void retryConnection() {
        mTopDisposable = getMovieList(mTopVm.getNextPage(), MovieEntries.TABLE_TOP_RATED);
        mPopDisposable = getMovieList(mPopVm.getNextPage(), MovieEntries.TABLE_POPULAR);
    }

    public void notifyPause() {
        insertToDb(mTopMoviesToDb, MovieEntries.TABLE_TOP_RATED);
        insertToDb(mPopMoviesToDb, MovieEntries.TABLE_POPULAR);
    }

    public void dispose() {
        disposeAllDisposables();
        unregisterListeners();
    }

    private void unregisterListeners() {
        mTopVm.unregisterTopPagesListener();
        mPopVm.unregisterPopPagesListener();
        mConnectionListener = null;
    }

    private void disposeAllDisposables() {
        disposeDisposable(mTopDisposable,
                mPopDisposable,
                mFavDisposable,
                mTAndRDisposable,
                mCachedTopQueryDisposable,
                mCachedPopQueryDisposable,
                mDeleteTopTableDisposable,
                mDeletePopTableDisposable);
    }

    private void disposeDisposable(Disposable... disposables) {
        for (Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) disposable.dispose();
        }
    }

    public void setMovieAndFetch(Movie movie) {
        mTrailerVm.clear();
        mReviewVm.clear();
        mDetailMovie = movie;
        mTAndRDisposable = getTrailersAndReviews();
    }

    public void refreshList(int listType) {
        if (!mIsOnline) return;
        switch (listType) {
            case 0:
                disposeDisposable(mTopDisposable,
                        mCachedTopQueryDisposable,
                        mDeleteTopTableDisposable);
                if (!mTopMoviesToDb.isEmpty()) mTopMoviesToDb = new ArrayList<>();
                mTopVm.clearPages(false);
                mDeleteTopTableDisposable = deleteTableAndRefresh(MovieEntries.TABLE_TOP_RATED);
                break;
            case 1:
                disposeDisposable(mPopDisposable,
                        mCachedPopQueryDisposable,
                        mDeletePopTableDisposable);
                if (!mPopMoviesToDb.isEmpty()) mPopMoviesToDb = new ArrayList<>();
                mPopVm.clearPages(false);
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

        if (mTopVm.isLastPageLoaded() && !mIsOnline) return;
        mTopDisposable = getMovieList(mTopVm.getNextPage(), MovieEntries.TABLE_TOP_RATED);
    }

    @Override
    public void getPopPages() {
        if (mPopVm.isLastPageLoaded() && !mIsOnline) return;
        mPopDisposable = getMovieList(mPopVm.getNextPage(), MovieEntries.TABLE_POPULAR);
    }

    private Disposable getMovieList(int pageNumber, String tableName) {
        return getMovieListCall(pageNumber, tableName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(apiResult -> handleApiSuccessCallback(apiResult, tableName),
                        throwable -> handleApiFailCallback(tableName));
    }

    private Disposable insertToDb(List<Movie> movies, String tableName) {
        return Observable.just(mDbHelper.batchInsertMovies(movies, tableName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (tableName.equals(MovieEntries.TABLE_TOP_RATED)) {
                        mTopMoviesToDb.clear();
                    } else {
                        mPopMoviesToDb.clear();
                    }
                });
    }

    private Disposable deleteTableAndRefresh(String tableName) {
        return Observable.just(mDbHelper.deleteAllFromTable(tableName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> getMovieList(1, tableName));
    }

    private Disposable queryForCachedMovieLists(final String tableName) {
        return Observable.just(mDbHelper.getAllMovies(tableName, MOVIE_DB_COLUMNS))
                .subscribeOn(Schedulers.io())
                .map(this::processCursor)
                .switchMap(movies -> {
                    if (movies.isEmpty()) {
                        if (tableName.equals(MovieEntries.TABLE_TOP_RATED)) {
                            mTopVm.clearPages(true);
                            return getMovieListCall(mTopVm.getNextPage(),
                                    MovieEntries.TABLE_TOP_RATED);
                        } else {
                            mPopVm.clearPages(true);
                            return getMovieListCall(mPopVm.getNextPage(),
                                    MovieEntries.TABLE_POPULAR);
                        }

                    } else {
                        if (tableName.equals(MovieEntries.TABLE_TOP_RATED)) {
                            mTopVm.setTopList(movies, true);
                        } else {
                            mPopVm.setPopList(movies, true);
                        }
                        return Observable.empty();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(apiResult -> handleApiSuccessCallback(apiResult, tableName),
                        throwable -> handleApiFailCallback(tableName));

    }

    private Observable<ApiResult> getMovieListCall(int pageNumber, String tableName) {
        return getClientForMovieList().create(TMDBApi.class)
                .getMovieList(tableName.equals(MovieEntries.TABLE_TOP_RATED) ? SORT_TOP : SORT_POP,
                        TMDB_API_KEY, LOCALE, pageNumber);
    }

    private void handleApiSuccessCallback(ApiResult apiResult, String tableName){
        apiResult.setListType(tableName);
        apiResult = setMoviePageNumbers(apiResult);

        if (apiResult != null && !apiResult.getMovies().isEmpty()) {
            if (tableName.equals(MovieEntries.TABLE_TOP_RATED)) {
                mTopMoviesToDb.addAll(apiResult.getMovies());
                mTopVm.setPages(apiResult);
                mTopVm.setTopList(apiResult.getMovies(), false);
            } else {
                mPopMoviesToDb.addAll(apiResult.getMovies());
                mPopVm.setPages(apiResult);
                mPopVm.setPopList(apiResult.getMovies(), false);
            }
        }
    }

    private void handleApiFailCallback(String tableName){
        if (tableName.equals(MovieEntries.TABLE_TOP_RATED)) {
            mTopVm.setTopList(new ArrayList<>(), false);
        } else mPopVm.setPopList(new ArrayList<>(), false);
    }

    @SuppressWarnings("ConstantConditions")
    private Disposable getFavList() {
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
    public void addToFavourites(Movie movie) {
        ContentResolver cr = mContext.getContentResolver();
        Observable.just(cr.insert(MovieEntries.FAVOURITES_CONTENT_URI, makeContentVals(movie)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uri -> mFavVm.addToList(movie), throwable -> Log.d("DataProvider", "addToFavourites: " + throwable.getMessage()));
    }

    @SuppressLint("CheckResult")
    public void deleteFromFavourites(Movie movie) {
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


    private Disposable getTrailersAndReviews() {
        return getClientForMovieList().create(TMDBApi.class)
                .getTrailersAndReviews(mDetailMovie.getMovieId(), TMDB_API_KEY, LOCALE, "videos,reviews")
                .subscribeOn(Schedulers.io())
                .switchMap(trailersAndReviews -> {
                    Reviews reviews = trailersAndReviews.getReviews();
                    Trailers trailers = trailersAndReviews.getTrailers();

                    mReviewsToViewModel = reviews.getReviewResults();
                    mTrailerVm.setTrailers(trailers.getTrailerResults());

                    if (reviews.getTotalPages() > 1) {
                        return getMoreReviews();
                    } else {
                        mReviewVm.setReviews(mReviewsToViewModel);
                        return Observable.empty();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(reviews -> {
                    if (!reviews.getReviewResults().isEmpty()) {
                        mReviewsToViewModel.addAll(reviews.getReviewResults());
                        mReviewVm.setReviews(mReviewsToViewModel);
                    } else mReviewVm.setReviews(mReviewsToViewModel);
                }, throwable -> {
                    mReviewVm.setReviews(
                            (mReviewsToViewModel != null && !mReviewsToViewModel.isEmpty()) ?
                                    mReviewsToViewModel : new ArrayList<>());
                    mTrailerVm.setTrailers(new ArrayList<>());
                });
    }

    /*
    As the API rarely returns more than one page of reviews. The class only calls for a second
    page if there are more than 1.
    Will make a similar listener for the reviews if the API gets more popular and the movies gets
    more reviews.
    */
    private Observable<Reviews> getMoreReviews() {
        return getClientForMovieList().create(TMDBApi.class)
                .getMoreReviews(mDetailMovie.getMovieId(), TMDB_API_KEY, LOCALE, 2);
    }

    private List<Movie> processCursor(Cursor cursor) {
        List<Movie> moviesFromCursor = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                moviesFromCursor.add(createMovieFromCursor(cursor, cursor.getPosition()));
            }
            cursor.close();
        }
        return moviesFromCursor;
    }

    //Connection Interceptor solution from:
    //https://medium.com/@tsaha.cse/advanced-retrofit2-part-1-network-error-handling-response-caching-77483cf68620
    private Retrofit getClientForMovieList() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new NetworkConnectionInterceptor() {
                    @Override
                    public boolean isInternetAvailable() {
                        mIsOnline = DataProvider.this.isInternetConnected();
                        return mIsOnline;
                    }

                    @Override
                    public void onInternetAvailable() {
                        if (mConnectionListener != null) {
                            mConnectionListener.onInternetAvailable();
                        }
                    }

                    @Override
                    public void onInternetUnavailable() {
                        if (mConnectionListener != null) {
                            mConnectionListener.onInternetUnavailable();
                        }
                    }
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_QUERY_API_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
    }

    private boolean isInternetConnected() {
        ConnectivityManager cm = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
