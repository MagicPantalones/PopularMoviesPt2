package io.magics.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;


import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.popularmovies.models.ApiResult;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.networkutils.TMDBApi;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static io.magics.popularmovies.networkutils.TMDBApiNetworkService.getClientForMovieList;


public class PosterActivity extends AppCompatActivity
        implements PosterAdapter.PosterClickHandler{

    int mPageNumber;
    TMDBApi.SortingMethod mSortMethod;
    PosterAdapter mPosterAdapter;
    Observable<ApiResult> mObservable;
    ApiResult mMovieListResponse;
    Disposable mDisposable;

    @BindView(R.id.rv_grid_recycler) RecyclerView mGridRecyclerView;
    @BindView(R.id.pb_loading) ProgressBar mMovieLoader;
    @BindView(R.id.iv_error) ImageView mErrorImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poster);
        ButterKnife.bind(this);

        mSortMethod = TMDBApi.SortingMethod.POPULAR;

        initRecycler();

        connectAndFetchData();
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

        hideGridStartLoad();

        final Boolean orientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, orientation ? 2 : 1);

        mPageNumber = 1;

        gridLayoutManager.setOrientation(orientation ? GridLayoutManager.VERTICAL : GridLayoutManager.HORIZONTAL);
        mGridRecyclerView.setLayoutManager(gridLayoutManager);
        mGridRecyclerView.setHasFixedSize(true);

        mPosterAdapter = new PosterAdapter(this);

        mGridRecyclerView.setAdapter(mPosterAdapter);

        mPosterAdapter.setEndListener(position ->  {
                mPageNumber += 1;
                mMovieLoader.setVisibility(View.VISIBLE);
                connectAndFetchData();
        });
    }

    /**
     * Checks for an active internet connection. If true, starts the AsyncTask to call the API else shows a toast.
     */

    public void connectAndFetchData(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        showGrid();

        if (ni != null && ni.isConnectedOrConnecting()){
            getMovieList(mSortMethod, mPageNumber);
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

    }

    public void getMovieList(TMDBApi.SortingMethod sortingMethod, int pageNumber){
        String apikey = getResources().getString(R.string.THE_MOVIE_DB_API_TOKEN);
        TMDBApi tmdbApi = getClientForMovieList().create(TMDBApi.class);
        mObservable = tmdbApi.getMovieList(sortingMethod, apikey, "en-US", pageNumber);

        mObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiResult>() {
                    @Override
                    public void onSubscribe(Disposable d) { mDisposable = d; }

                    @Override
                    public void onNext(ApiResult apiResult) { mMovieListResponse = apiResult; }

                    @Override
                    public void onError(Throwable e) {
                        if (mDisposable != null && !mDisposable.isDisposed()){
                            mDisposable.dispose();
                        }
                        if (e instanceof HttpException){
                            HttpException response = (HttpException) e;
                            int code = response.code();
                            Toast.makeText(PosterActivity.this, "Error Loading data. Code returned: " + code, Toast.LENGTH_LONG).show();
                        }
                        showErrorImage();
                    }

                    @Override
                    public void onComplete() {
                        mPosterAdapter.setMovieData(mMovieListResponse.getMovies());
                        mMovieLoader.setVisibility(View.GONE);
                    }
                });
    }

    //Utility methods that shows/hides views on connecting, error or complete.

    public void showGrid(){
        mErrorImage.setVisibility(View.GONE);
        mGridRecyclerView.setVisibility(View.VISIBLE);
    }

    public void hideGridStartLoad(){
        mGridRecyclerView.setVisibility(View.INVISIBLE);
        mMovieLoader.setVisibility(View.VISIBLE);
    }

    public void showErrorImage(){
        mErrorImage.setVisibility(View.VISIBLE);
        mGridRecyclerView.setVisibility(View.GONE);
    }

    //On click method to start the details activity.
    @Override
    public void onClick(Movie movie, View v) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        Bundle extras = new Bundle();
        extras.putParcelable("movie", movie);
        extras.putInt("width", v.getMeasuredWidth());
        extras.putInt("height", v.getMeasuredHeight());
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
                    initRecycler();
                    connectAndFetchData();
                }
                else item.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {

        if (mDisposable != null && !mDisposable.isDisposed()) mDisposable.dispose();

        super.onDestroy();
    }

}
