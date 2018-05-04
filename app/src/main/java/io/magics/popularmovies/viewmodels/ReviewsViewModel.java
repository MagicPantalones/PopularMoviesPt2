package io.magics.popularmovies.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import io.magics.popularmovies.models.ReviewResult;

public class ReviewsViewModel extends ViewModel {

    public MutableLiveData<List<ReviewResult>> mReviews = new MutableLiveData<>();

    public void setReviews(List<ReviewResult> reviews){
        if (mReviews.getValue() == null) {
            mReviews.setValue(reviews);
        }
    }

    public void clear(){ mReviews = new MutableLiveData<>(); }

}
