package io.magics.popularmovies.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import io.magics.popularmovies.models.TrailerResult;

public class TrailersViewModel extends ViewModel {

    public MutableLiveData<List<TrailerResult>> mTrailers = new MutableLiveData<>();

    public void setTrailers(List<TrailerResult> trailers){
        if (mTrailers.getValue() == null) mTrailers.setValue(trailers);
    }

    public void clear(){ mTrailers = new MutableLiveData<>(); }

}
