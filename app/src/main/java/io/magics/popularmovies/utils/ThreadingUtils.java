package io.magics.popularmovies.utils;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.magics.popularmovies.models.Movie;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static io.magics.popularmovies.database.FavouritesDBHelper.*;
import static io.magics.popularmovies.utils.MovieUtils.createMovieFromCursor;

public class ThreadingUtils {

    private static final String TAG = ThreadingUtils.class.getSimpleName();

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

    public interface CursorResponseHandler{
        void movieListFromCursor(List<Movie> movies);
    }

    public interface DeleteQueryResponse {
        void deleteResponse(int success);
    }

    public interface InsertQueryResponse {
        void insertResponse(Boolean success, Uri favUri);
    }

    public static boolean checkIfFav(int movieId, List<Integer> idList){
        if (idList == null) return false;
        for (int i : idList){
            if (i == movieId) return true;
        }
        return false;
    }

    public static Disposable queryForFavouriteMovies(Context context, final CursorResponseHandler responseHandler){
        ContentResolver cr = context.getContentResolver();
        String sortOrder = FavouritesEntry._ID + " ASC";
        //noinspection ConstantConditions
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
                .subscribe(responseHandler::movieListFromCursor);
    }

    public static void addToFavourites(Context context, Movie movie, InsertQueryResponse insertHandler){
        ContentResolver cr = context.getContentResolver();
        Observable.just(makeContentVals(movie))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(contentValues -> {
                    Uri uri = cr.insert(FavouritesEntry.FAVOURITES_CONTENT_URI, contentValues);
                    insertHandler.insertResponse(uri != null, uri);
                }, throwable -> Log.d(TAG, "addToFavourites: " + throwable.getMessage()));
    }

    public static void deleteFromFavourites(Context context, Movie movie, DeleteQueryResponse deleteHandler){
        ContentResolver cr = context.getContentResolver();
        Observable.just(cr.delete(
                FavouritesEntry.FAVOURITES_CONTENT_URI,
                FavouritesEntry.COLUMN_MOVIE_ID + " = " + Integer.toString(movie.getMovieId()),
                null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleteHandler::deleteResponse,
                        throwable -> Log.d(TAG, "deleteFromFavourites: " + throwable.getMessage()));
    }

    public static ContentValues makeContentVals(Movie movie){
        ContentValues cv = new ContentValues();

        cv.put(FavouritesEntry.COLUMN_POSTER_PATH, movie.getPosterUrl());
        cv.put(FavouritesEntry.COLUMN_OVERVIEW, movie.getOverview());
        cv.put(FavouritesEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        cv.put(FavouritesEntry.COLUMN_MOVIE_ID, movie.getMovieId());
        cv.put(FavouritesEntry.COLUMN_TITLE, movie.getTitle());
        cv.put(FavouritesEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        cv.put(FavouritesEntry.COLUMN_COLOR_PATH, movie.getShadowInt());
        return cv;
    }

}
