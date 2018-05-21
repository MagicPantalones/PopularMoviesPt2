package io.magics.popularmovies.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import io.magics.popularmovies.models.ApiResult;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.MovieUtils;

public class PopListViewModel extends ViewModel {

    public interface GetMorePopPagesListener{
        void getPopPages();
    }

    public final MutableLiveData<List<Movie>> mPopList = new MutableLiveData<>();

    private GetMorePopPagesListener mNotifyListener;

    private int mLastPage = 1;
    private int mCurrentPage = 1;
    private boolean mIsLastPageSet = false;
    private boolean mIsLastPageLoaded = false;

    public void setPages(ApiResult apiResult){
        if (!mIsLastPageSet){
            mLastPage = apiResult.getTotalPages();
            mIsLastPageSet = true;
        }
        mCurrentPage = apiResult.getPage();
        mIsLastPageLoaded = mCurrentPage + 1 > mLastPage;
    }

    public int getCurrentPage(){ return mCurrentPage; }

    public boolean isLastPageLoaded(){ return mIsLastPageLoaded; }

    public void setPopList(List<Movie> movies){
        List<Movie> tempMovies = mPopList.getValue() != null ? mPopList.getValue() : new ArrayList<>();

        if (!MovieUtils.checkForDuplicateList(tempMovies, movies)) {
            tempMovies.addAll(movies);
            mPopList.setValue(tempMovies);
        }
    }

    public void notifyGetMorePopPages(){
        if (mNotifyListener != null) mNotifyListener.getPopPages();
    }

    public void addGetMorePopPagesListener(GetMorePopPagesListener listener){
        mNotifyListener = listener;
    }

    public void unregisterPopPagesListener(){
        mNotifyListener = null;
    }

}
