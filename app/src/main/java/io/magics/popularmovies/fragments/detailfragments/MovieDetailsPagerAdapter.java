package io.magics.popularmovies.fragments.detailfragments;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import io.magics.popularmovies.models.Movie;

public class MovieDetailsPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 4;

    private Movie mMovie;
    private String mTransitionId;


    public MovieDetailsPagerAdapter(Movie movie, String transitionId, FragmentManager fm) {
        super(fm);
        mMovie = movie;
        mTransitionId = transitionId;
    }


    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return MovieDetailsPoster.newInstance(mMovie, mTransitionId);
            case 1:
                return MovieDetailsOverview.newInstance(mMovie);
            case 2:
                return new MovieDetailsTrailers();
            case 3:
                return new MovieDetailsReviews();
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

}
