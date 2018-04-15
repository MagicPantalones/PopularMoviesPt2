package io.magics.popularmovies.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.magics.popularmovies.models.Movie;

public class FavListViewModel extends ViewModel {

    public MutableLiveData<List<Movie>> mFavList = new MutableLiveData<>();
    private List<Integer> mMoviesId = new ArrayList<>();

    public void setFavList(List<Movie> favList) {

        for (Movie m : favList) {
            mMoviesId.add(m.getMovieId());
        }

        mFavList.setValue(favList);
    }

    public void addToList(Movie movie){
        List<Movie> mL = mFavList.getValue() != null ? mFavList.getValue() : new ArrayList<>();
        mL.add(movie);
        mMoviesId.add(movie.getMovieId());
        mFavList.setValue(mL);
    }

    public void removeFromList(Movie movie){
        if (mFavList.getValue() == null) {
            Log.e("FavListViewModel",
                    "Tried to remove from a FavViewModel without a favourite list",
                    new NullPointerException());
            return;
        }
        List<Movie> mL = mFavList.getValue();
        mL.remove(movie);
        mMoviesId.remove(movie.getMovieId());
        mFavList.setValue(mL);
    }

    public boolean checkIfFavourite(int movieId){
        for (int i : mMoviesId){
            if (movieId == i) return true;
        }
        return false;
    }

}
