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

public class FavouritesProvider extends ContentProvider {

    private static final int FAVOURITE_ID = 100;
    private static final int FAVOURITE_DIR = 101;

    private  FavouritesDBHelper mDbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String auth = FavouritesDBHelper.AUTHORITY;
        matcher.addURI(auth, FavouritesDBHelper.PATH_FAVOURITES + "/#", FAVOURITE_ID);
        matcher.addURI(auth, FavouritesDBHelper.PATH_FAVOURITES, FAVOURITE_DIR);
        return matcher;
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new FavouritesDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        switch (sUriMatcher.match(uri)){
            case FAVOURITE_ID:
                cursor = db.query(FavouritesDBHelper.FavouritesEntry.TABLE_NAME_FAVOURITES,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case FAVOURITE_DIR:
                cursor = db.query(FavouritesDBHelper.FavouritesEntry.TABLE_NAME_FAVOURITES,
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
        switch (sUriMatcher.match(uri)){
            case FAVOURITE_DIR:
                id = mDbHelper.getWritableDatabase().insert(FavouritesDBHelper.FavouritesEntry.TABLE_NAME_FAVOURITES, null, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI " + uri);
        }
        if (id == -1){
            throw new UnsupportedOperationException("Nothing inserted at " + uri);
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
                        FavouritesDBHelper.NAME,
                        selection,
                        selectionArgs);
                break;
            case FAVOURITE_DIR:
                del = mDbHelper.getWritableDatabase().delete(
                        FavouritesDBHelper.NAME,
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
