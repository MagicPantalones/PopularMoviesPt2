package io.magics.popularmovies.viewmodels;

import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import io.magics.popularmovies.models.Movie;

public class AnimationHelperViewModel extends ViewModel {

    private List<Movie> mMovies = new ArrayList<>();
    private int mAdapterPosition;
    private int mAdapterOffset;

    public void saveAdapterValues(List<Movie> movies, int adapterPosition, int adapterOffset){
        mMovies = movies;
        mAdapterPosition = adapterPosition;
        mAdapterOffset = adapterOffset;
    }

    public List<Movie> getMovies() { return mMovies; }

    public int getAdapterPosition() { return mAdapterPosition; }

    public int getAdapterOffset() { return mAdapterOffset; }

}
