package io.magics.popularmovies.networkutils;


import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.magics.popularmovies.BuildConfig;
import io.magics.popularmovies.models.ApiResult;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.magics.popularmovies.networkutils.TMDBApiNetworkService.ImageSize.SIZE_DEFAULT;
import static io.magics.popularmovies.networkutils.TMDBApiNetworkService.ImageSize.SIZE_MEDIUM;

public class TMDBApiNetworkService {
    private static final String BASE_QUERY_API_URL = "https://api.themoviedb.org/3/";
    private static final String BASE_QUERY_IMG_URL = "https://image.tmdb.org/t/p/";
    private static final String TMDB_API_KEY = BuildConfig.TMDB_API_KEY;

    private Disposable mDisposable;
    private ApiResult mApiResult;

    public TMDBApiNetworkService(){}

    public interface TMDBCallbackResult{
        void onSuccess(ApiResult apiResult, Disposable d);
        void onError(int error, String message, Throwable e);
    }

    public void callTMDB(TMDBApi.SortingMethod sortingMethod, int pageNumber, final TMDBCallbackResult callback){
        TMDBApi tmdbClient = getClientForMovieList().create(TMDBApi.class);
        tmdbClient.getMovieList(sortingMethod, TMDB_API_KEY, "en-US", pageNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(ApiResult apiResult) {
                        mApiResult = apiResult;
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException){
                            int eCode = ((HttpException) e).code();
                            String eMessage = ((HttpException) e).message();
                            callback.onError(eCode, eMessage, e);
                        } else {
                            callback.onError(-1, "callTMDB non-networkException", e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        callback.onSuccess(mApiResult, mDisposable);
                    }
                });
    }

    private static Retrofit getClientForMovieList() {

        return new Retrofit.Builder()
                .baseUrl(BASE_QUERY_API_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static String posterUrlConverter(ImageSize imageSize, String posterPath) {
        return Uri.parse(BASE_QUERY_IMG_URL).buildUpon()
                .appendEncodedPath(imageSize.toString())
                .appendEncodedPath(posterPath)
                .build().toString();
    }

    public static ImageSize getOptimalImgSize(Context context){
        float density = context.getResources().getDisplayMetrics().density;
        return density >= 3.0 ? SIZE_MEDIUM : SIZE_DEFAULT;
    }

    public enum ImageSize{
        @SerializedName("w500")
        SIZE_LARGE("w500"),
        SIZE_MEDIUM("w342"),
        SIZE_SMALL("w92"),
        SIZE_DEFAULT("w185");

        private final String retText;

        ImageSize(final String retText) { this.retText = retText; }


        @Override
        public String toString() { return retText; }
    }
}
