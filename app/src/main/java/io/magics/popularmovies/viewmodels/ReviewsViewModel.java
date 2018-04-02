package io.magics.popularmovies.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import io.magics.popularmovies.models.ReviewResult;

public class ReviewsViewModel extends ViewModel {

    private MutableLiveData<List<ReviewResult>> mReviews = new MutableLiveData<>();

}
