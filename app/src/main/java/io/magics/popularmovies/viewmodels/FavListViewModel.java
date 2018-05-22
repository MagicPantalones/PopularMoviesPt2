package io.magics.popularmovies.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.magics.popularmovies.models.Movie;

public class FavListViewModel extends ViewModel {

    public final MutableLiveData<List<Movie>> mFavList = new MutableLiveData<>();
    private final List<Integer> mMoviesId = new ArrayList<>();

    public void setFavList(List<Movie> favList) {

        //The ID's from the Movies to be assigned to the LiveData List.
        //This is to avoid calling the value of the LiveData to only check an ID.
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

    /**
     * Will compare the provided ID with an ID in {@link #mMoviesId}
     * @param movieId the provided Movie ID
     * @return true if the movie is in the list or false otherwise.
     */
    public boolean checkIfFavourite(int movieId){
        for (int i : mMoviesId){
            if (movieId == i) return true;
        }
        return false;
    }

}
