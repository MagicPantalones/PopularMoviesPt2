package io.magics.popularmovies;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.stetho.Stetho;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.models.ReviewResult;
import io.magics.popularmovies.models.Reviews;
import io.magics.popularmovies.models.TrailerResult;
import io.magics.popularmovies.models.Trailers;
import io.magics.popularmovies.networkutils.ApiUtils;
import io.magics.popularmovies.networkutils.TMDBApi.SortingMethod;
import io.magics.popularmovies.utils.ThreadingUtils;
import io.reactivex.disposables.Disposable;

import static io.magics.popularmovies.networkutils.ApiUtils.isConnected;

public class MovieListsActivity extends AppCompatActivity {

    private static final String TAB_FRAG_TAG = "tabFragTag";

    FragmentListTabLayout mTabFrag;

    List<ReviewResult> mReviews = new ArrayList<>();
    List<TrailerResult> mTrailers = new ArrayList<>();
    List<Movie> mFavMovieList = new ArrayList<>();
    List<Movie> mTopMovieList = new ArrayList<>();
    List<Movie> mPopMovieList = new ArrayList<>();

    int mTopPageNum = 1;
    int mTopLastPage = -1;
    int mPopPageNum = 1;
    int mPopLastPage = -1;
    int mReviewPageNum = 1;
    int mReviewLastPage = -1;

    Disposable mTopDisposable;
    Disposable mPopDisposable;
    Disposable mFavDisposable;
    Disposable mTAndRDisposable;
    Disposable mMoreRevDisposable;

    TopRatedResultsListener mTopListener;
    PopularResultsListener mPopListener;
    FavouriteResultsListener mFavListener;
    DetailsListeners mDetailsListeners;

    boolean mConnected = true;


    public interface TopRatedResultsListener {
        void topMoviesResultDelivery(List<Movie> movies);
    }

    public interface PopularResultsListener{
        void popularMoviesDelivery(List<Movie> movies);
    }

    public interface FavouriteResultsListener{
        void favouritesResultDelivery(List<Movie> movies);
    }

    public interface DetailsListeners{
        void onTrailerFetchFinished(Trailers trailers);
        void onReviewFetchFinished(Reviews reviews);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_lists);
        ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);

        FragmentManager fm = getSupportFragmentManager();
        mTabFrag = (FragmentListTabLayout) fm.findFragmentByTag(TAB_FRAG_TAG);

        if (mTabFrag == null) {
            mTabFrag = FragmentListTabLayout.instantiateFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container_main, mTabFrag, TAB_FRAG_TAG).commit();
        }

        getInitFavouritesList();

        mConnected = isConnected(this);
        if (mConnected) {
            getTopRatedList();
            getPopularList();
        }
    }

    @Override
    protected void onDestroy() {
        if (mTopDisposable != null && !mTopDisposable.isDisposed()) mTopDisposable.dispose();
        if (mPopDisposable != null && !mPopDisposable.isDisposed()) mPopDisposable.dispose();
        if (mFavDisposable != null && !mFavDisposable.isDisposed()) mFavDisposable.dispose();
        if (mTAndRDisposable != null && !mTAndRDisposable.isDisposed()) mTAndRDisposable.dispose();
        if (mMoreRevDisposable != null && !mMoreRevDisposable.isDisposed()) mMoreRevDisposable.dispose();
        super.onDestroy();
    }

    public boolean hasRequestedFromNetwork(){
        return mConnected;
    }

    private void getTopRatedList() {
        if (mTopLastPage != -1 && mTopPageNum >= mTopLastPage) return;
        mTopDisposable = ApiUtils.callApiForMovieList(SortingMethod.TOP_RATED, mTopPageNum,
                apiResult -> {
                    mTopMovieList.addAll(apiResult.getMovies());
                    mTopPageNum = apiResult.getPage();
                    if (mTopLastPage == -1) mTopLastPage = apiResult.getTotalPages();
                    if (mTopListener != null) {
                        mTopListener.topMoviesResultDelivery(apiResult.getMovies());
                    }
                });
    }

    public void getMoreTopRated(){
        mConnected = isConnected(this);
        if (mConnected) {
            mTopPageNum += 1;
            getTopRatedList();
        }
    }

    public List<Movie> getMovieLists(int tabPage){
        switch (tabPage){
            case 1: return mTopMovieList;
            case 2: return mPopMovieList;
            default: return new ArrayList<>();
        }
    }

    private void getPopularList() {
        if (mPopLastPage != -1 && mPopPageNum >= mPopLastPage) return;
        mPopDisposable = ApiUtils.callApiForMovieList(SortingMethod.POPULAR, mPopPageNum,
                apiResult -> {
                    mPopMovieList.addAll(apiResult.getMovies());
                    mPopPageNum = apiResult.getPage();
                    if (mPopLastPage == -1) mPopLastPage = apiResult.getTotalPages();
                    if (mPopListener != null) {
                        mPopListener.popularMoviesDelivery(apiResult.getMovies());
                    }
                });
    }

    public void getMorePopular(){
        mConnected = isConnected(this);
        if (mConnected) {
            mPopPageNum += 1;
            getPopularList();
        }
    }

    private void getInitFavouritesList() {
        mFavDisposable = ThreadingUtils.queryForFavouriteMovies(this,
                movies -> {
                    mFavMovieList = movies;
                    if (mFavListener != null) mFavListener.favouritesResultDelivery(movies);
                });
    }

    public void notifyReadyForFav(){
        if (mFavMovieList.isEmpty()){
            getInitFavouritesList();
            return;
        }
        mFavListener.favouritesResultDelivery(mFavMovieList);
    }

    private void getTrailersAndReviews(int movieId) {
        mTAndRDisposable = ApiUtils.callApiForTrailersAndReviews(movieId,
                results -> {
                    mReviews.addAll(results.getReviews().getReviewResults());
                    mTrailers.addAll(results.getTrailers().getTrailerResults());
                    mReviewPageNum = results.getReviews().getPage();

                    if (mReviewLastPage == -1) mReviewLastPage = results.getReviews().getTotalPages();

                    if (mDetailsListeners != null){
                        mDetailsListeners.onReviewFetchFinished(results.getReviews());
                        mDetailsListeners.onTrailerFetchFinished(results.getTrailers());
                    }

                });
    }

    private void getMoreReviews(int movieId) {
        mReviewPageNum += 1;
        if (mReviewLastPage != -1 && mReviewPageNum >= mReviewLastPage) return;
        mMoreRevDisposable = ApiUtils.callForMoreReviews(movieId, mReviewPageNum,
                result -> {
                    mReviews.addAll(result.getReviewResults());
                    mReviewPageNum = result.getPage();
                });
    }

    public void addToFavourites(Movie movie) {
        ThreadingUtils.addToFavourites(this, movie, (success, favUri) -> {
            if (success) {
                movie.setFavouriteUri(favUri);
                mFavMovieList.add(movie);
                mFavListener.favouritesResultDelivery(mFavMovieList);
            }
        });
    }

    public void deleteFromFavourites(Movie movie) {
        ThreadingUtils.deleteFromFavourites(this, movie, success -> {
            if (success >= 1) {
                mFavMovieList.remove(movie);
                mFavListener.favouritesResultDelivery(mFavMovieList);
            }
        });
    }

    public void showMovieDetailsFrag(Movie movie) {
        boolean favCheck = false;
        getTrailersAndReviews(movie.getMovieId());

        for (Movie m : mFavMovieList){
            if (m.getMovieId().equals(movie.getMovieId())){
                favCheck = true;
                break;
            }
        }

        MovieDetailsFragment frag = MovieDetailsFragment.newInstance(movie, favCheck);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.container_main, frag);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void registerTopListener(TopRatedResultsListener listener) {
        mTopListener = listener;
    }

    public void unRegisterTopListener() {
        mTopListener = null;
    }

    public void registerPopListener(PopularResultsListener listener){
        mPopListener = listener;
    }

    public void unRegisterPopListener(){
        mPopListener = null;
    }

    public void registerFavListener(FavouriteResultsListener listener){
        mFavListener = listener;
    }

    public void unRegisterFavListener(){
        mFavListener = null;
    }

    public void registerDetailListeners(DetailsListeners listeners){ mDetailsListeners = listeners; }

    public void unRegisterDetailListeners() { mDetailsListeners = null; }

    public void notifyMovieListChange(Movie movie) {
        int i;
        if (mFavMovieList.contains(movie)) {
            i = mFavMovieList.indexOf(movie);
            mFavMovieList.set(i, movie);
        }
        if (mPopMovieList.contains(movie)) {
            i = mPopMovieList.indexOf(movie);
            mPopMovieList.set(i, movie);
        }
        if (mTopMovieList.contains(movie)) {
            i = mTopMovieList.indexOf(movie);
            mTopMovieList.set(i, movie);
        }
    }

}
