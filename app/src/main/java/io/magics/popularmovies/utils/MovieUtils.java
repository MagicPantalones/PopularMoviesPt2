package io.magics.popularmovies.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;


/**
 * MovieDB JSON parser
 * Created by Erik on 17.02.2018.
 */

public class MovieUtils {

    private static final String TAG = MovieUtils.class.getSimpleName();

    private MovieUtils(){}

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
}
