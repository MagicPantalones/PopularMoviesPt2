package io.magics.popularmovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;


import com.facebook.stetho.Stetho;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.popularmovies.database.FavouritesDBHelper;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.networkutils.TMDBApi;
import io.magics.popularmovies.networkutils.ApiUtils;
import io.magics.popularmovies.utils.MovieUtils;
import io.reactivex.disposables.Disposable;

import static io.magics.popularmovies.networkutils.ApiUtils.callApi;
import static io.magics.popularmovies.utils.ThreadingUtils.queryFavouritesCursor;

public class PosterActivity extends AppCompatActivity
        implements PosterAdapter.PosterClickHandler{

    private static final String TAG = PosterActivity.class.getSimpleName();

    FavouritesDBHelper dbh = new FavouritesDBHelper(this);

    int mPageNumber;
    TMDBApi.SortingMethod mSortMethod;
    Boolean isDataFromCursor;
    PosterAdapter mPosterAdapter;
    io.magics.popularmovies.models.ApiResult mMovieListResponse;
    Disposable mNetworkDisposable;

    @BindView(R.id.rv_grid_recycler) RecyclerView mGridRecyclerView;
    @BindView(R.id.pb_loading) ProgressBar mMovieLoader;
    @BindView(R.id.iv_error) ImageView mErrorImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poster);
        ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);

        mSortMethod = TMDBApi.SortingMethod.POPULAR;

    }

    @Override
    protected void onStart() {
        super.onStart();

        initRecycler();
        if (isDataFromCursor == null || !isDataFromCursor) {
            connectAndFetchData();
        }

    }

    @Override
    protected void onDestroy() {
        if (mNetworkDisposable != null && !mNetworkDisposable.isDisposed()) mNetworkDisposable.dispose();

        super.onDestroy();
    }

    /**
     * Initiates the recycler view.
     *
     * Hides the recycler and shows the loader.
     * Sets the Layout Manager, Adapter and sets a listener that will call the API again for the next page
     * from the API.
     *
     */
    public void initRecycler(){
        final Boolean orientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, orientation ? 2 : 1);
        mGridRecyclerView.setVisibility(View.INVISIBLE);
        mMovieLoader.setVisibility(View.VISIBLE);

        mPageNumber = 1;

        gridLayoutManager.setOrientation(orientation ? GridLayoutManager.VERTICAL : GridLayoutManager.HORIZONTAL);
        mGridRecyclerView.setLayoutManager(gridLayoutManager);
        mGridRecyclerView.setHasFixedSize(true);


        mPosterAdapter = new PosterAdapter(this);

            mGridRecyclerView.setAdapter(mPosterAdapter);
            if (isDataFromCursor == null || !isDataFromCursor) {
                mPosterAdapter.setEndListener(position -> {
                    mPageNumber += 1;
                    mMovieLoader.setVisibility(View.VISIBLE);
                    connectAndFetchData();
                });
            }
    }

    /**
     * Checks for an active internet connection. If true, starts the AsyncTask to call the API else shows a toast.
     */

    public void connectAndFetchData(){

        if (ApiUtils.isConnected(PosterActivity.this)){
            MovieUtils.hideAndShowView(mMovieLoader, mGridRecyclerView);
            getMoviesFromNetwork(mSortMethod, mPageNumber);
        } else {
            MovieUtils.hideAndShowView(mMovieLoader, mGridRecyclerView);
            getMoviesFromFavourites();
        }

    }

    public void getMoviesFromNetwork(TMDBApi.SortingMethod sortingMethod, int pageNumber){
        mNetworkDisposable = callApi(sortingMethod, pageNumber, apiResult -> {
            mMovieListResponse = apiResult;
            mPosterAdapter.setMovieData(mMovieListResponse.getMovies(), mPosterAdapter.getItemCount(), false);
            MovieUtils.hideAndShowView(mGridRecyclerView, mMovieLoader);
        });
    }

    public void getMoviesFromFavourites(){
        queryFavouritesCursor(this, movieList -> {
            mPosterAdapter.setMovieData(movieList, mPosterAdapter.getItemCount(),true);
            MovieUtils.hideAndShowView(mGridRecyclerView, mMovieLoader);
        });
    }

    //On click method to start the details activity.
    @Override
    public void onClick(Movie movie, int position) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        Bundle extras = new Bundle();
        extras.putParcelable("movie", movie);
        intent.putExtras(extras);
        startActivity(intent);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sorting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mi_popular:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    isDataFromCursor = false;
                    mSortMethod = TMDBApi.SortingMethod.POPULAR;
                    initRecycler();
                    connectAndFetchData();
                }
                else item.setChecked(true);
                return true;
            case R.id.mi_top_rated:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    mSortMethod = TMDBApi.SortingMethod.TOP_RATED;
                    isDataFromCursor = false;
                    initRecycler();
                    connectAndFetchData();
                }
                else item.setChecked(true);
                return true;
            case R.id.mi_favourites:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    isDataFromCursor = true;
                    initRecycler();
                    getMoviesFromFavourites();
                }
                else item.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
