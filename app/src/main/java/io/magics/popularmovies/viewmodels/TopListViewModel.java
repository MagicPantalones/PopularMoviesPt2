package io.magics.popularmovies.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import io.magics.popularmovies.models.ApiResult;
import io.magics.popularmovies.models.Movie;

public class TopListViewModel extends ViewModel {

    public interface GetMoreTopPagesListener {
        void getTopPages();
    }

    public MutableLiveData<List<Movie>> mTopList = new MutableLiveData<>();
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
        mCurrentPage = apiResult.getPage();
        mIsLastPageLoaded = mCurrentPage + 1 > mLastPage;
    }

    public int getCurrentPage(){ return mCurrentPage; }

    public boolean isLastPageLoaded(){ return mIsLastPageLoaded; }

    public void setTopList(List<Movie> movies){
        mTopList.setValue(movies);
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
