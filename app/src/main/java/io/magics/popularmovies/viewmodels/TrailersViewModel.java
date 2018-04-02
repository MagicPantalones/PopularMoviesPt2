package io.magics.popularmovies.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import io.magics.popularmovies.models.TrailerResult;

public class TrailersViewModel extends ViewModel {

    private MutableLiveData<List<TrailerResult>> mTrailers = new MutableLiveData<>();

}
