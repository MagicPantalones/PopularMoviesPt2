package io.magics.popularmovies.fragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class MovieListsPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 3;
    private String[] tabTitles = new String[] {"Top Rated", "Popular", "Favourites"};

    public MovieListsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    

    @Override
    public Fragment getItem(int position) {
        return MovieListFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }


}
