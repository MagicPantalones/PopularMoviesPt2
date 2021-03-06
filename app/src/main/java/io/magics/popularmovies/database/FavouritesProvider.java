package io.magics.popularmovies.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
public class FavouritesProvider extends ContentProvider {

    private static final int FAVOURITE_ID = 100;
    private static final int FAVOURITE_DIR = 101;

    private PopularMoviesDBHelper mDbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String auth = PopularMoviesDBHelper.AUTHORITY;
        matcher.addURI(auth, PopularMoviesDBHelper.PATH_FAVOURITES + "/#", FAVOURITE_ID);
        matcher.addURI(auth, PopularMoviesDBHelper.PATH_FAVOURITES, FAVOURITE_DIR);
        return matcher;
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new PopularMoviesDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        switch (sUriMatcher.match(uri)){
            case FAVOURITE_ID:
                cursor = db.query(PopularMoviesDBHelper.MovieEntries.TABLE_FAVOURITES,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case FAVOURITE_DIR:
                cursor = db.query(PopularMoviesDBHelper.MovieEntries.TABLE_FAVOURITES,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                return null;
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        Long id;

        if (sUriMatcher.match(uri) == FAVOURITE_DIR) {
            id = mDbHelper.getWritableDatabase().insert(PopularMoviesDBHelper.MovieEntries.TABLE_FAVOURITES, null, values);

        } else {
            throw new UnsupportedOperationException("Unknown URI " + uri);
        }
        if (id == -1){
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int del;

        switch (sUriMatcher.match(uri)){
            case FAVOURITE_ID:
                del = mDbHelper.getWritableDatabase().delete(
                        PopularMoviesDBHelper.MovieEntries.TABLE_FAVOURITES,
                        selection,
                        selectionArgs);
                break;
            case FAVOURITE_DIR:
                del = mDbHelper.getWritableDatabase().delete(
                        PopularMoviesDBHelper.MovieEntries.TABLE_FAVOURITES,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        if (del != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return del;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
