package io.magics.popularmovies.networkutils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.magics.popularmovies.BuildConfig;
import io.magics.popularmovies.models.ApiResult;
import io.magics.popularmovies.models.Reviews;
import io.magics.popularmovies.models.TrailersAndReviews;
import io.magics.popularmovies.networkutils.TMDBApi.SortingMethod;
import io.magics.popularmovies.utils.MovieUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiUtils {
    private static final String BASE_QUERY_API_URL = "https://api.themoviedb.org/3/";
    private static final String BASE_QUERY_IMG_URL = "https://image.tmdb.org/t/p/";
    private static final String TMDB_API_KEY = BuildConfig.TMDB_API_KEY;


    private ApiUtils(){}

    public interface ApiCallResult {
        void onSuccess(ApiResult apiResult);
    }

    public interface TrailersAndReviewsResult{
        void onCompleted(TrailersAndReviews results);
    }

    public interface MoreReviewsResult{
        void onCompleted(Reviews result);
    }

    public static Disposable callApiForMovieList(SortingMethod sortingMethod, int pageNumber, final ApiCallResult callback){
        return getClientForMovieList().create(TMDBApi.class)
                .getMovieList(sortingMethod, TMDB_API_KEY, "en-US", pageNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback::onSuccess);
    }

    public static Disposable callApiForTrailersAndReviews(String movieId, final TrailersAndReviewsResult result){
        return getClientForMovieList().create(TMDBApi.class)
                .getTrailersAndReviews(movieId, TMDB_API_KEY, "en-US", "videos,reviews")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result::onCompleted);
    }

    public static Disposable callForMoreReviews(String movieId, int pagenumber, final MoreReviewsResult result){
        return getClientForMovieList().create(TMDBApi.class)
                .getMoreReviews(movieId, TMDB_API_KEY, "en-US", pagenumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result::onCompleted);
    }

    private static Retrofit getClientForMovieList() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return new Retrofit.Builder()
                .baseUrl(BASE_QUERY_API_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static String posterUrlConverter(MovieUtils.ImageSize imageSize, String posterPath) {
        return Uri.parse(BASE_QUERY_IMG_URL).buildUpon()
                .appendEncodedPath(imageSize.toString())
                .appendEncodedPath(posterPath)
                .build().toString();
    }



    public static boolean isConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }
}
