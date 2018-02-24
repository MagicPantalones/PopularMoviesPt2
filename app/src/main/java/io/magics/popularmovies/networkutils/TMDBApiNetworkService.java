package io.magics.popularmovies.networkutils;

//TODO When exporting to repo for pt.2 remember to add gradle.properties to git ignore file

import android.content.Context;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.magics.popularmovies.networkutils.TMDBApiNetworkService.ImageSize.SIZE_DEFAULT;
import static io.magics.popularmovies.networkutils.TMDBApiNetworkService.ImageSize.SIZE_MEDIUM;

public class TMDBApiNetworkService {
    private static final String BASE_QUERY_API_URL = "https://api.themoviedb.org/3/";
    private static final String BASE_QUERY_IMG_URL = "https://image.tmdb.org/t/p/";

    private TMDBApiNetworkService(){}

    public static Retrofit getClientForMovieList() {

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
