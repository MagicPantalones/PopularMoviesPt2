package io.magics.popularmovies.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import io.magics.popularmovies.models.ApiResult;
import io.magics.popularmovies.models.Movie;

import static io.magics.popularmovies.utils.MovieUtils.getLiveDataList;

public class TopListViewModel extends ViewModel {

    public interface GetMoreTopPagesListener {
        void getTopPages();
    }

    public final MutableLiveData<List<Movie>> mTopList = new MutableLiveData<>();
    private GetMoreTopPagesListener mNotifyListener;

    private int mLastPage = 1;
    private int mCurrentPage = 1;
    private boolean mIsLastPageSet = false;
    private boolean mIsLastPageLoaded = false;

    public void setPages(ApiResult apiResult){
        if (!mIsLastPageSet){
            mLastPage = apiResult.getTotalPages();
            mIsLastPageSet = true;
        }
    }

    public void clearPages(){
        mLastPage = 1;
        mIsLastPageSet = false;
        mCurrentPage = 1;
        mIsLastPageLoaded = false;
        mTopList.setValue(new ArrayList<>());
    }

    public int getCurrentPage(){ return mCurrentPage; }

    public boolean isLastPageLoaded(){ return mIsLastPageLoaded; }

    public void setTopList(List<Movie> movies, boolean fromDb){
        List<Movie> tempMovies = mTopList.getValue() != null && !fromDb ?
                mTopList.getValue() : new ArrayList<>();

        tempMovies = getLiveDataList(tempMovies, movies);

        mCurrentPage = tempMovies.get(tempMovies.size() - 1).getPageNumber();
        mIsLastPageLoaded = mIsLastPageSet && mCurrentPage + 1 > mLastPage;

        mTopList.setValue(tempMovies);
    }

    public void notifyGetMoreTopPages(){
        if (mNotifyListener != null) mNotifyListener.getTopPages();
    }

    public void addGetMoreTopPagesListener(GetMoreTopPagesListener listener){
        mNotifyListener = listener;
    }

    public void unregisterTopPagesListener(){
        mNotifyListener = null;
    }

}
