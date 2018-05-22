package io.magics.popularmovies.networkutils;


import io.magics.popularmovies.models.ApiResult;
import io.magics.popularmovies.models.Reviews;
import io.magics.popularmovies.models.TrailersAndReviews;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * An interface needed for the Retrofit client and RxJava
 * @see DataProvider
 * @see io.magics.popularmovies.utils.MovieUtils
 */
interface TMDBApi {

    @GET("movie/{sortingMethod}")
    Observable<ApiResult> getMovieList(@Path("sortingMethod") String sort,
                                                   @Query("api_key") String api,
                                                   @Query("language") String locale,
                                                   @Query("page") int page);
    @GET("movie/{movieId}")
    Observable<TrailersAndReviews> getTrailersAndReviews(@Path("movieId") int movieId,
                                                         @Query("api_key") String api,
                                                         @Query("language") String locale,
                                                         @Query("append_to_response") String appendString);
    @GET("movie/{movieId}/reviews")
    Observable<Reviews> getMoreReviews(@Path("movieId") int movieId,
                                       @Query("api_key") String api,
                                       @Query("language") String locale,
                                       @Query("page") int page);

}


