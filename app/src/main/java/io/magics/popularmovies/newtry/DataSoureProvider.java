package io.magics.popularmovies.newtry;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import io.magics.popularmovies.BuildConfig;
import io.magics.popularmovies.database.FavouritesDBHelper;
import io.magics.popularmovies.database.FavouritesDBHelper.FavouritesEntry;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.networkutils.TMDBApi;
import io.magics.popularmovies.viewmodels.FavListViewModel;
import io.magics.popularmovies.viewmodels.PopListViewModel;
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

import static io.magics.popularmovies.utils.MovieUtils.createMovieFromCursor;

public class DataSoureProvider {

    public static final String[] FAVOURITES_COLUMNS = {
            FavouritesEntry._ID,
            FavouritesEntry.COLUMN_POSTER_PATH,
            FavouritesEntry.COLUMN_OVERVIEW,
            FavouritesEntry.COLUMN_RELEASE_DATE,
            FavouritesEntry.COLUMN_MOVIE_ID,
            FavouritesEntry.COLUMN_TITLE,
            FavouritesEntry.COLUMN_VOTE_AVERAGE,
            FavouritesEntry.COLUMN_COLOR_PATH
    };

    public static final int ITEM_ID = 0;
    public static final int POSTER_I = 1;
    public static final int OVERVIEW_I = 2;
    public static final int REL_DATE_I = 3;
    public static final int MOVIE_ID_I = 4;
    public static final int TITLE_I = 5;
    public static final int VOTE_AV_I = 6;
    public static final int COLOR_I = 7;


    private static final String SORT_TOP = "top_rated";
    private static final String SORT_POP = "popular";
    private static final int INIT_PAGE_NUM = 1;
    private static final String BASE_QUERY_API_URL = "https://api.themoviedb.org/3/";
    private static final String TMDB_API_KEY = BuildConfig.TMDB_API_KEY;
    private static final String LOCALE = "en-US";

    private final Context mContext;
    private Disposable mTopDisposable;
    private Disposable mPopDisposable;
    private Disposable mFavDisposable;

    private final TopListViewModel mTopVm;
    private final PopListViewModel mPopVm;
    private final FavListViewModel mFavVm;
    private TrailersViewModel mTrailerVm;
    private ReviewsViewModel mReviewVm;



    public DataSoureProvider(Context context,
                             TopListViewModel topVm,
                             PopListViewModel popVm,
                             FavListViewModel favVm){
        this.mContext = context;
        this.mTopVm = topVm;
        this.mPopVm = popVm;
        this.mFavVm = favVm;
    }

    public void initialiseApp(){
        mTopDisposable = getTopList(INIT_PAGE_NUM);
        mPopDisposable = getPopList(INIT_PAGE_NUM);
        mFavDisposable = getFavList();
    }

    public void getMoreForTop(){
        if (mTopVm.isLastPageLoaded()) return;
        mTopDisposable = getTopList(mTopVm.getCurrentPage() + 1);
    }

    public void getMoreForPop(){
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
                });
    }

    private Disposable getPopList(int pageNumber){
        return getClientForMovieList().create(TMDBApi.class)
                .getMovieList(SORT_POP, TMDB_API_KEY, LOCALE, pageNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback -> {
                    mPopVm.setPages(callback);
                    mPopVm.setPopList(callback.getMovies());
                });
    }

    private Retrofit getClientForMovieList() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return new Retrofit.Builder()
                .baseUrl(BASE_QUERY_API_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean isConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
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

    private ContentValues makeContentVals(Movie movie){
        ContentValues cv = new ContentValues();

        cv.put(FavouritesEntry.COLUMN_POSTER_PATH, movie.getPosterUrl());
        cv.put(FavouritesEntry.COLUMN_OVERVIEW, movie.getOverview());
        cv.put(FavouritesEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        cv.put(FavouritesEntry.COLUMN_MOVIE_ID, movie.getMovieId());
        cv.put(FavouritesEntry.COLUMN_TITLE, movie.getTitle());
        cv.put(FavouritesEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        cv.put(FavouritesEntry.COLUMN_COLOR_PATH, Integer.toString(movie.getShadowInt()));

        return cv;
    }

}
