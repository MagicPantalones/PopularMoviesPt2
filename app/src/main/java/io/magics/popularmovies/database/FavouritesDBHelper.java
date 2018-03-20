package io.magics.popularmovies.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

@SuppressWarnings("WeakerAccess")
public class FavouritesDBHelper extends SQLiteOpenHelper {


    public static final String NAME = "favourites.db";
    public static final String AUTHORITY = "io.magics.popularmovies";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_FAVOURITES = "favourites";

    private static final int DB_VERSION = 5;

    private static final String DB_HELP_TEXT_MIDDLE = " TEXT, ";
    private static final String DB_HELP_INT_MIDDLE = " INTEGER NOT NULL, ";

    public FavouritesDBHelper(Context context){
        super(context, NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVOURITES_TABLE =
                "CREATE TABLE " + FavouritesEntry.TABLE_NAME_FAVOURITES + " (" +

                        FavouritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FavouritesEntry.COLUMN_POSTER_PATH + DB_HELP_TEXT_MIDDLE +
                        FavouritesEntry.COLUMN_OVERVIEW + DB_HELP_TEXT_MIDDLE +
                        FavouritesEntry.COLUMN_RELEASE_DATE + DB_HELP_TEXT_MIDDLE +
                        FavouritesEntry.COLUMN_MOVIE_ID + DB_HELP_INT_MIDDLE +
                        FavouritesEntry.COLUMN_TITLE + DB_HELP_TEXT_MIDDLE +
                        FavouritesEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                        FavouritesEntry.COLUMN_COLOR_PATH + DB_HELP_INT_MIDDLE +

                        " UNIQUE (" + FavouritesEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_FAVOURITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavouritesEntry.TABLE_NAME_FAVOURITES);
        onCreate(db);
    }

    public static final class FavouritesEntry implements BaseColumns {

        private FavouritesEntry(){}

        public static final Uri FAVOURITES_CONTENT_URI = BASE_URI.buildUpon()
                        .appendPath(PATH_FAVOURITES)
                        .build();

        public static final String TABLE_NAME_FAVOURITES = "favourites";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_POSTER_PATH = "poster_url";

        public static final String COLUMN_COLOR_PATH = "shadow_color";

    }
}
