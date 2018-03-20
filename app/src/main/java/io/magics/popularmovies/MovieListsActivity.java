package io.magics.popularmovies;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.models.ReviewResult;
import io.magics.popularmovies.models.TrailerResult;
import io.magics.popularmovies.networkutils.ApiUtils;
import io.magics.popularmovies.networkutils.TMDBApi.SortingMethod;
import io.magics.popularmovies.utils.ThreadingUtils;
import io.reactivex.disposables.Disposable;

import static io.magics.popularmovies.networkutils.ApiUtils.isConnected;

public class MovieListsActivity extends AppCompatActivity {

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

    List<MovieResultsListener> mMovieResultListeners = new ArrayList<>();
    TrailersAndReviewsListener mTrailersAndReviewsListener;
    AddOrRemoveFavHandler mAddOrRemoveHandler;

    boolean mConnected = true;


    public interface MovieResultsListener {
        void resultDelivery(List<Movie> movies, MovieResultType type);
    }

    public interface TrailersAndReviewsListener {
        void trailersResult(List<TrailerResult> trailers);

        void reviewsResult(List<ReviewResult> reviews);

        void moreReviewsResult(List<ReviewResult> reviews);
    }

    public interface AddOrRemoveFavHandler {
        void onAdded();

        void onRemoved();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_lists);
        ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);

        if (savedInstanceState == null) {
            FragmentListTabLayout frag = FragmentListTabLayout.instantiateFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container_main, frag).commit();
        }

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
        if (mTopLastPage != -1 && mTopPageNum + 1 < mTopLastPage) mTopLastPage += 1;
        if (mTopLastPage != -1 && mTopPageNum >= mTopLastPage) return;
        mTopDisposable = ApiUtils.callApiForMovieList(SortingMethod.TOP_RATED, mTopPageNum,
                apiResult -> {
                    mTopMovieList.addAll(apiResult.getMovies());
                    mTopPageNum = apiResult.getPage();
                    if (mTopLastPage == -1) mTopLastPage = apiResult.getTotalPages();
                    if (!mMovieResultListeners.isEmpty()) {
                        for (MovieResultsListener l : mMovieResultListeners) {
                            l.resultDelivery(apiResult.getMovies(), MovieResultType.TOP_RATED);
                        }
                    }
                });

    }

    public void getMoreTopRated(){
        mConnected = isConnected(this);
        if (mConnected) getTopRatedList();
    }

    private void getPopularList() {
        if (mPopLastPage != -1 && mPopPageNum + 1 < mPopLastPage) mPopPageNum += 1;
        if (mPopLastPage != -1 && mPopPageNum >= mPopLastPage) return;
        mPopDisposable = ApiUtils.callApiForMovieList(SortingMethod.POPULAR, mPopPageNum,
                apiResult -> {
                    mPopMovieList.addAll(apiResult.getMovies());
                    mPopPageNum = apiResult.getPage();
                    if (mPopLastPage == -1) mPopLastPage = apiResult.getTotalPages();
                    if (!mMovieResultListeners.isEmpty()) {
                        for (MovieResultsListener l : mMovieResultListeners) {
                            l.resultDelivery(apiResult.getMovies(), MovieResultType.POPULAR);
                        }
                    }
                });
    }

    public void getMorePopular(){
        mConnected = isConnected(this);
        if (mConnected) getPopularList();
    }

    public void getFavouritesList() {
        mFavDisposable = ThreadingUtils.queryForFavouriteMovies(this,
                movies -> {
                    mFavMovieList = movies;
                    if (!mMovieResultListeners.isEmpty()) {
                        for (MovieResultsListener l : mMovieResultListeners) {
                            l.resultDelivery(movies, MovieResultType.FAVOURITES);
                        }
                    }
                });
    }

    public void getTrailersAndReviews(int movieId) {
        mTAndRDisposable = ApiUtils.callApiForTrailersAndReviews(movieId,
                results -> {
                    mReviews.addAll(results.getReviews().getReviewResults());
                    mTrailers.addAll(results.getTrailers().getTrailerResults());
                    mReviewPageNum = results.getReviews().getPage();
                    if (mReviewLastPage == -1)
                        mReviewLastPage = results.getReviews().getTotalPages();
                    if (mTrailersAndReviewsListener != null) {
                        mTrailersAndReviewsListener.trailersResult(mTrailers);
                        mTrailersAndReviewsListener.reviewsResult(mReviews);
                    }
                });
    }

    public void getMoreReviews(int movieId) {
        if (mReviewLastPage != -1 && mReviewPageNum + 1 < mReviewLastPage) mReviewPageNum += 1;
        if (mReviewLastPage != -1 && mReviewPageNum >= mReviewLastPage) return;
        mMoreRevDisposable = ApiUtils.callForMoreReviews(movieId, mReviewPageNum,
                result -> {
                    mReviews.addAll(result.getReviewResults());
                    mReviewPageNum = result.getPage();
                    if (mTrailersAndReviewsListener != null) {
                        mTrailersAndReviewsListener.moreReviewsResult(result.getReviewResults());
                    }
                });
    }

    public void addToFavourites(Movie movie) {
        ThreadingUtils.addToFavourites(this, movie, (success, favUri) -> {
            if (success) {
                movie.setFavouriteUri(favUri);
                mFavMovieList.add(movie);
                if (mAddOrRemoveHandler != null) {
                    mAddOrRemoveHandler.onAdded();
                }
                Toast.makeText(this, "Movie added to favourites", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "Failed adding movie to favourites", Toast.LENGTH_SHORT).show();
        });
    }

    public void deleteFromFavourites(Movie movie) {
        ThreadingUtils.deleteFromFavourites(this, movie, success -> {
            if (success >= 1) {
                mFavMovieList.remove(movie);
                if (mAddOrRemoveHandler != null) {
                    mAddOrRemoveHandler.onRemoved();
                }
                Toast.makeText(this, "Movie removed from favourites", Toast.LENGTH_SHORT).show();
            } else Toast.makeText(this, "Failed to delete movie", Toast.LENGTH_SHORT).show();
        });
    }

    public void showMovieDetailsFrag(Movie movie) {

        getTrailersAndReviews(movie.getMovieId());

        MovieDetailsFragment frag = MovieDetailsFragment.newInstance(movie, mFavMovieList.contains(movie));
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.container_main, frag);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void registerListListeners(MovieResultsListener listener) {
        mMovieResultListeners.add(listener);
    }

    public void unRegisterListListeners(MovieResultsListener listener) {
        mMovieResultListeners.remove(listener);
    }

    public void registerDetailsListeners(TrailersAndReviewsListener tListener, AddOrRemoveFavHandler aListener) {
        mTrailersAndReviewsListener = tListener;
        mAddOrRemoveHandler = aListener;
    }

    public void unRegisterDetailsListeners() {
        mTrailersAndReviewsListener = null;
        mAddOrRemoveHandler = null;
    }

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

    enum MovieResultType {
        TOP_RATED,
        POPULAR,
        FAVOURITES
    }

}
