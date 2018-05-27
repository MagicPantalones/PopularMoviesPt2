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
    private int mCurrentPage = 0;
    private boolean mIsLastPageSet = false;
    private boolean mIsLastPageLoaded = false;

    public void setPages(ApiResult apiResult){
        if (!mIsLastPageSet){
            mLastPage = apiResult.getTotalPages();
            mIsLastPageSet = true;
        }
    }

    public void clearPages(boolean fromBackground){
        mLastPage = 1;
        mIsLastPageSet = false;
        mCurrentPage = 0;
        mIsLastPageLoaded = false;
        if (fromBackground) mPopList.postValue(new ArrayList<>());
        else mPopList.setValue(new ArrayList<>());
    }

    public boolean isLastPageLoaded(){ return mIsLastPageLoaded; }

    public int getNextPage() { return mCurrentPage + 1; }

    public void setPopList(List<Movie> movies, boolean fromDb){
        List<Movie> tempMovies = mPopList.getValue() != null && !fromDb ?
                mPopList.getValue() : new ArrayList<>();

        tempMovies = getLiveDataList(tempMovies, movies);

        if (!tempMovies.isEmpty()) {
            mCurrentPage = tempMovies.get(tempMovies.size() - 1).getPageNumber();
        }
        mIsLastPageLoaded = mIsLastPageSet && mCurrentPage + 1 > mLastPage;

        if (fromDb) mPopList.postValue(tempMovies);
        else mPopList.setValue(tempMovies);
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
