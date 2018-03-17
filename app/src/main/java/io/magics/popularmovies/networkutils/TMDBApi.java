package io.magics.popularmovies.networkutils;


import io.magics.popularmovies.models.ApiResult;
import io.magics.popularmovies.models.Reviews;
import io.magics.popularmovies.models.TrailersAndReviews;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TMDBApi {
    @GET("movie/{sortingMethod}")
    Observable<ApiResult> getMovieList(@Path("sortingMethod") SortingMethod sort,
                                                   @Query("api_key") String api,
                                                   @Query("language") String locale,
                                                   @Query("page") int page);
    @GET("movie/{movieId}")
    Observable<TrailersAndReviews> getTrailersAndReviews(@Path("movieId") String movieId,
                                                         @Query("api_key") String api,
                                                         @Query("language") String locale,
                                                         @Query("append_to_response") String appendString);
    @GET("movie/{movieId}/reviews")
    Observable<Reviews> getMoreReviews(@Path("movieId") String movieId,
                                       @Query("api_key") String api,
                                       @Query("language") String locale,
                                       @Query("page") int page);

    enum SortingMethod{
        POPULAR("popular"),
        TOP_RATED("top_rated");

        private final String retText;

        SortingMethod(final String retText) { this.retText = retText; }


        @Override
        public String toString() { return retText; }
    }
}


