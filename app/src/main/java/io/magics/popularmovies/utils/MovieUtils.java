package io.magics.popularmovies.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.google.gson.annotations.SerializedName;

import io.magics.popularmovies.database.PopularMoviesDBHelper.MovieEntries;
import io.magics.popularmovies.models.ApiResult;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.networkutils.DataProvider;

import static io.magics.popularmovies.utils.MovieUtils.ImageSize.SIZE_DEFAULT;
import static io.magics.popularmovies.utils.MovieUtils.ImageSize.SIZE_MEDIUM;


/**
 * <h6>Utility class for project.</h6>
 * <p>Some methods and enums that was used in multiple classes</p>
 *
 * <p>However, some method thats only used once like {@link #createMovieFromCursor(Cursor, int)},
 * is kept here as the methods could be static.</p>
 *
 */
public class MovieUtils {

    private static final String TAG = MovieUtils.class.getSimpleName();
    private static final String YOUTUBE_THUMB_BASE_URL = "http://img.youtube.com/vi/";
    private static final String BASE_QUERY_IMG_URL = "https://image.tmdb.org/t/p/";

    private static final int POSTER_I = 1;
    private static final int OVERVIEW_I = 2;
    private static final int REL_DATE_I = 3;
    private static final int MOVIE_ID_I = 4;
    private static final int TITLE_I = 5;
    private static final int VOTE_AV_I = 6;
    private static final int COLOR_I = 7;
    private static final int PAGE_NUM_I = 8;


    private MovieUtils(){}

    public static void toggleViewVisibility(View... views){
        for (View v : views){
            v.setVisibility(v.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        }
    }

    public static ApiResult setMoviePageNumbers(ApiResult apiResult) {
        List<Movie> movies = apiResult.getMovies();
        int pageNumber = apiResult.getPage();
        for (Movie m : movies) {
            m.setPageNumber(pageNumber);
        }

        apiResult.setMovies(movies);
        return apiResult;
    }


    public static Movie createMovieFromCursor(Cursor cursor, int position){
        Movie retMovie = new Movie();
        cursor.moveToPosition(position);

        retMovie.setPosterUrl(cursor.getString(POSTER_I));
        retMovie.setOverview(cursor.getString(OVERVIEW_I));
        retMovie.setReleaseDate(cursor.getString(REL_DATE_I));
        retMovie.setMovieId(cursor.getInt(MOVIE_ID_I));
        retMovie.setTitle(cursor.getString(TITLE_I));
        retMovie.setVoteAverage(cursor.getDouble(VOTE_AV_I));
        retMovie.setShadowInt(Integer.valueOf(cursor.getString(COLOR_I)));
        retMovie.setPageNumber(cursor.getInt(PAGE_NUM_I));

        return retMovie;
    }

    public static List<Movie> getLiveDataList(List<Movie> oldList, List<Movie> newList){

        if (!oldList.isEmpty() && !newList.isEmpty()) {
            int oldListPageNum = oldList.get(oldList.size() - 1).getPageNumber();
            int newListFirstPageNum = newList.get(0).getPageNumber();

            if (oldListPageNum + 1 == newListFirstPageNum){
                oldList.addAll(newList);
                return oldList;
            }
        }

        if (oldList.isEmpty() && !newList.isEmpty()) return newList;
        return oldList;
    }

    //Formats date to "MMM dd(ordinal number), yyyy

    public static String formatDate(String dateString){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat sdf1 = new SimpleDateFormat("MMMM", Locale.ENGLISH);
        String[] dateSplit = dateString.split("-");

        try{
            Date month = sdf.parse(dateString);
            String day = dateSplit[2] + getOrdinal(dateSplit[2]);

            return sdf1.format(month) + " " + day + ", " + dateSplit[0];
        } catch (ParseException e){
            Log.e(TAG, "formatDate: ", e);
            return dateString;
        }

    }

    /*copied way to format the correct ordinal from Greg Mattes answer on
    https://stackoverflow.com/questions/4011075/how-do-you-format-the-day-of-the-month-to-say-11th-21st-or-23rd-ordinal
    */

    private static String getOrdinal(String dayString){
        int dayInt = Integer.parseInt(dayString);
        if (dayInt >= 11 && dayInt <= 13){
            return "th";
        }
        switch (dayInt % 10){
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    public static ImageSize getOptimalImgSize(Context context){
        float density = context.getResources().getDisplayMetrics().density;
        return density >= 3.0 ? SIZE_MEDIUM : SIZE_DEFAULT;
    }

    public static String posterUrlConverter(ImageSize imageSize, String posterPath) {
        return Uri.parse(BASE_QUERY_IMG_URL).buildUpon()
                .appendEncodedPath(imageSize.toString())
                .appendEncodedPath(posterPath)
                .build().toString();
    }

    public static String youtubeStillUrlConverter(String youtubeKey){
        return Uri.parse(YOUTUBE_THUMB_BASE_URL).buildUpon()
                .appendEncodedPath(youtubeKey)
                .appendPath("0.jpg")
                .build().toString();
    }

    public static ContentValues makeContentVals(Movie movie){
        ContentValues cv = new ContentValues();

        cv.put(MovieEntries.COLUMN_POSTER_PATH, movie.getPosterUrl());
        cv.put(MovieEntries.COLUMN_OVERVIEW, movie.getOverview());
        cv.put(MovieEntries.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        cv.put(MovieEntries.COLUMN_MOVIE_ID, movie.getMovieId());
        cv.put(MovieEntries.COLUMN_TITLE, movie.getTitle());
        cv.put(MovieEntries.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        cv.put(MovieEntries.COLUMN_COLOR_PATH, Integer.toString(movie.getShadowInt()));
        cv.put(MovieEntries.COLUMN_PAGE_NUMBER, movie.getPageNumber());

        return cv;
    }

    @SuppressWarnings("unused")
    public enum ImageSize{
        @SerializedName("w500")
        SIZE_MEDIUM("w342"),
        SIZE_DEFAULT("w185");

        private final String retText;

        ImageSize(final String retText) { this.retText = retText; }


        @Override
        public String toString() { return retText; }
    }

    public enum ScrollDirection{
        SCROLL_UP,
        SCROLL_DOWN
    }


}
