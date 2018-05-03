package io.magics.popularmovies.fragments.detailfragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import io.magics.popularmovies.models.Movie;

public class MovieDetailsPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 4;

    private static final String[] pageTitles = new String[]{
            "Poster", "Overview", "Trailers", "Reviews"
    };

    private Movie mMovie;


    public MovieDetailsPagerAdapter(Movie movie, FragmentManager fm) {
        super(fm);
        mMovie = movie;
    }


    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return MovieDetailsPoster.newInstance(mMovie);
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
