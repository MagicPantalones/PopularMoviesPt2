package io.magics.popularmovies.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import io.magics.popularmovies.models.ApiResult;
import io.magics.popularmovies.models.Movie;

public class TopListViewModel extends ViewModel {

    private MutableLiveData<List<Movie>> mTopList = new MutableLiveData<>();

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
        if (mTopList.getValue() == null || mTopList.getValue().isEmpty()){
            mTopList.setValue(movies);
        } else {
            List<Movie> movieList = mTopList.getValue();
            movieList.addAll(movies);
            mTopList.setValue(movieList);
        }
    }

}
