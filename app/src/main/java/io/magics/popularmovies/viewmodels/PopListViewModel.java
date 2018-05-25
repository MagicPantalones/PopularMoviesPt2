package io.magics.popularmovies.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import io.magics.popularmovies.models.ApiResult;
import io.magics.popularmovies.models.Movie;

import static io.magics.popularmovies.utils.MovieUtils.getLiveDataList;

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
    }

    public void clearPages(){
        mLastPage = 1;
        mIsLastPageSet = false;
        mCurrentPage = 1;
        mIsLastPageLoaded = false;
        mPopList.setValue(new ArrayList<>());
    }

    public int getCurrentPage(){ return mCurrentPage; }

    public boolean isLastPageLoaded(){ return mIsLastPageLoaded; }

    public void setPopList(List<Movie> movies, boolean fromDb){
        List<Movie> tempMovies = mPopList.getValue() != null && !fromDb ?
                mPopList.getValue() : new ArrayList<>();

        tempMovies = getLiveDataList(tempMovies, movies);

        mCurrentPage = tempMovies.get(tempMovies.size() - 1).getPageNumber();
        mIsLastPageLoaded = mIsLastPageSet && mCurrentPage + 1 > mLastPage;

        mPopList.setValue(tempMovies);
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
