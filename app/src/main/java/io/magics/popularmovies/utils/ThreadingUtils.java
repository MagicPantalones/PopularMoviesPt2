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

    public static final String[] FAVOURITES_COLUMNS = {
            FavouritesEntry._ID,
            FavouritesEntry.COLUMN_POSTER_PATH,
            FavouritesEntry.COLUMN_OVERVIEW,
            FavouritesEntry.COLUMN_RELEASE_DATE,
            FavouritesEntry.COLUMN_MOVIE_ID,
            FavouritesEntry.COLUMN_TITLE,
            FavouritesEntry.COLUMN_BACKDROP_PATH,
            FavouritesEntry.COLUMN_VOTE_AVERAGE,
            FavouritesEntry.COLUMN_VOTE_COUNT
    };
    public static final int ITEM_ID = 0;
    public static final int POSTER_I = 1;
    public static final int OVERVIEW_I = 2;
    public static final int REL_DATE_I = 3;
    public static final int MOVIE_ID_I = 4;
    public static final int TITLE_I = 5;
    public static final int BACKDROP_I = 6;
    public static final int VOTE_AV_I = 7;
    public static final int VOTE_CO_I = 8;

    public interface CursorResponseHandler{
        void onReturnedCursor(List<Movie> movies);
    }

    public interface IsFavouriteHandler {
        void onIsFavouriteResponse(int id);
    }

    public interface FavouritesQueryHandler{
        void onQuerySuccess(List<Integer> movieIdList);
    }

    public static Disposable queryForFavourites(Context context, final FavouritesQueryHandler queryHandler){
        ContentResolver cr = context.getContentResolver();
        String sortOrder = FavouritesEntry._ID + " ASC";

        //noinspection ConstantConditions
        return Observable.just(cr.query(FavouritesEntry.FAVOURITES_CONTENT_URI,
                FAVOURITES_COLUMNS,
                FavouritesEntry.COLUMN_MOVIE_ID,
                null,
                sortOrder))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(cursor -> {
                    if (cursor != null) {
                        List<Integer> retList = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            retList.add(MOVIE_ID_I);
                        }
                        cursor.close();
                        return retList;
                    }
                    return new ArrayList<Integer>(0);
                })
                .subscribe(queryHandler::onQuerySuccess,
                        t -> Log.d("LIST-GETTER", t.getMessage()));
    }

    public static void queryFavouritesCursor(Context context, final CursorResponseHandler responseHandler){
        ContentResolver cr = context.getContentResolver();
        String sortOrder = FavouritesEntry._ID + " ASC";
        //noinspection ConstantConditions
        Observable.just(cr.query(FavouritesEntry.FAVOURITES_CONTENT_URI,
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
                    return null;
                })
                .subscribe(responseHandler::onReturnedCursor);
    }

    public static boolean addToFavourites(Context context, Movie movie, List<Integer> movieIds){
        Observable<Boolean> obs;

        ContentResolver cr = context.getContentResolver();
        for (int i : movieIds){
            if (i == movie.getMovieId()){
                obs = Observable.just(cr.delete(
                        ContentUris.withAppendedId(FavouritesEntry.FAVOURITES_CONTENT_URI, movie.getItemId()),
                        null,
                        null))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(id -> (id < 0));
                return obs.blockingSingle();
            }
        }

        obs = Observable.just(makeContentVals(movie))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(vals -> {
                    Uri uriCheck = cr.insert(FavouritesEntry.FAVOURITES_CONTENT_URI, vals);
                    return uriCheck != null;
                });
        return obs.blockingSingle();
    }

    public static ContentValues makeContentVals(Movie movie){
        ContentValues cv = new ContentValues();

        cv.put(FavouritesEntry.COLUMN_POSTER_PATH, movie.getPosterUrl());
        cv.put(FavouritesEntry.COLUMN_OVERVIEW, movie.getOverview());
        cv.put(FavouritesEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        cv.put(FavouritesEntry.COLUMN_MOVIE_ID, movie.getMovieId());
        cv.put(FavouritesEntry.COLUMN_TITLE, movie.getTitle());
        cv.put(FavouritesEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
        cv.put(FavouritesEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        cv.put(FavouritesEntry.COLUMN_VOTE_COUNT, movie.getVoteCount());
        return cv;
    }

}
