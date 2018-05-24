package io.magics.popularmovies.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.List;

import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.MovieUtils;

@SuppressWarnings("WeakerAccess")
public class PopularMoviesDBHelper extends SQLiteOpenHelper {

    private static final String TAG = PopularMoviesDBHelper.class.getSimpleName();

    public static final String NAME = "popularmovies.db";
    public static final String AUTHORITY = "io.magics.popularmovies";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_FAVOURITES = "favourites";

    //Decided to rename DB file and class. So begining from db version 1.
    //So far there are only me and reviewer who have installed app so I have not decided to get rid
    //of the old favourites.db file.
    private static final int DB_VERSION = 1;

    private static final String DB_HELP_TEXT_MIDDLE = " TEXT, ";
    private static final String DB_HELP_INT_MIDDLE = " INTEGER NOT NULL, ";

    public PopularMoviesDBHelper(Context context) {
        super(context, NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVOURITES_TABLE = MovieEntries.getCreateTableFavourites();

        final String SQL_CREATE_TOP_RATED_TABLE = MovieEntries.getCreateTableTopRated();

        final String SQL_CREATE_POPULAR_TABLE = MovieEntries.getCreateTablePopular();


        db.execSQL(SQL_CREATE_FAVOURITES_TABLE);
        db.execSQL(SQL_CREATE_TOP_RATED_TABLE);
        db.execSQL(SQL_CREATE_POPULAR_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //If any columns are added or removed, remember to use "ALTER" instead of "DROP"
        //Tutorial and explanation https://thebhwgroup.com/blog/how-android-sqlite-onupgrade
        //Sql script implementation:
        //https://riggaroo.co.za/android-sqlite-database-use-onupgrade-correctly/

        Log.d(TAG, "Updating from ver." + oldVersion + " to ver." + newVersion);

        if (oldVersion < 1) {
            db.execSQL("DROP TABLE IF EXISTS " + MovieEntries.TABLE_FAVOURITES);
            onCreate(db);
        }
    }

    public Cursor getAllMovies(final String tableName, String[] projection) {
        String sortOrder = MovieEntries._ID + " ASC";
        try {
            return getReadableDatabase().query(tableName,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    sortOrder);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            return getReadableDatabase().query(tableName, projection,
                    null, null, null, null, null);
        }
    }

    public long batchInsertMovies(List<Movie> movies, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        long rowId = -1;
        try {
            db.beginTransaction();
            for (Movie movie : movies) {
                rowId = db.insert(tableName, null, MovieUtils.makeContentVals(movie));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return rowId;
    }

    public int deleteAllFromTable(String tableName) {
        return getWritableDatabase().delete(tableName, "1", null);
    }


    public static class MovieEntries implements BaseColumns {

        private MovieEntries() {/* hide */}

        public static final Uri FAVOURITES_CONTENT_URI = BASE_URI.buildUpon()
                .appendPath(PATH_FAVOURITES)
                .build();

        public static final String TABLE_FAVOURITES = "favourites";
        public static final String TABLE_POPULAR = "popular";
        public static final String TABLE_TOP_RATED = "top_rated";


        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_POSTER_PATH = "poster_url";

        public static final String COLUMN_COLOR_PATH = "shadow_color";

        private static String getMovieColumns(String tableName) {
            return "CREATE TABLE " + tableName + " (" +

                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_POSTER_PATH + DB_HELP_TEXT_MIDDLE +
                    COLUMN_OVERVIEW + DB_HELP_TEXT_MIDDLE +
                    COLUMN_RELEASE_DATE + DB_HELP_TEXT_MIDDLE +
                    COLUMN_MOVIE_ID + DB_HELP_INT_MIDDLE +
                    COLUMN_TITLE + DB_HELP_TEXT_MIDDLE +
                    COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                    COLUMN_COLOR_PATH + DB_HELP_TEXT_MIDDLE +

                    " UNIQUE (" + COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";
        }

        private static String getCreateTableFavourites() {
            return getMovieColumns(TABLE_FAVOURITES);
        }

        private static String getCreateTablePopular() {
            return getMovieColumns(TABLE_TOP_RATED);
        }

        private static String getCreateTableTopRated() {
            return getMovieColumns(TABLE_POPULAR);
        }
    }
}
