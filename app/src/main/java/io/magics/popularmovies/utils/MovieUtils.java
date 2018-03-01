package io.magics.popularmovies.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;

import com.google.gson.annotations.SerializedName;

import io.magics.popularmovies.models.Movie;

import static io.magics.popularmovies.utils.MovieUtils.ImageSize.SIZE_DEFAULT;
import static io.magics.popularmovies.utils.MovieUtils.ImageSize.SIZE_MEDIUM;


/**
 * Helper Utils for project
 * Created by Erik on 17.02.2018.
 */

public class MovieUtils {

    private static final String TAG = MovieUtils.class.getSimpleName();

    private MovieUtils(){}

    public static void hideAndShowView(View viewToShow, View viewToHide){
        viewToShow.setVisibility(View.VISIBLE);
        viewToHide.setVisibility(View.GONE);
    }

    public static Movie createMovieFromCursor(Cursor cursor, int position){
        Movie retMovie = new Movie();
        cursor.moveToPosition(position);

        retMovie.setItemId(cursor.getInt(ThreadingUtils.ITEM_ID));
        retMovie.setPosterUrl(cursor.getString(ThreadingUtils.POSTER_I));
        retMovie.setOverview(cursor.getString(ThreadingUtils.OVERVIEW_I));
        retMovie.setReleaseDate(cursor.getString(ThreadingUtils.REL_DATE_I));
        retMovie.setMovieId(cursor.getInt(ThreadingUtils.MOVIE_ID_I));
        retMovie.setTitle(cursor.getString(ThreadingUtils.TITLE_I));
        retMovie.setBackdropPath(cursor.getString(ThreadingUtils.BACKDROP_I));
        retMovie.setVoteAverage(cursor.getDouble(ThreadingUtils.VOTE_AV_I));
        retMovie.setVoteCount(cursor.getInt(ThreadingUtils.VOTE_CO_I));

        return retMovie;
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
