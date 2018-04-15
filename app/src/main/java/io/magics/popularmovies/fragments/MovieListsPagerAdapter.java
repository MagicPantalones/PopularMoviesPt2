package io.magics.popularmovies.fragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import io.magics.popularmovies.fragments.listfragments.ListFavouritesFragment;
import io.magics.popularmovies.fragments.listfragments.ListPopularFragment;
import io.magics.popularmovies.fragments.listfragments.ListTopRatedFragment;


public class MovieListsPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 3;
    private String[] tabTitles = new String[] {"Top Rated", "Popular", "Favourites"};

    private ListTopRatedFragment mTopFrag;
    private ListPopularFragment mPopFrag;
    private ListFavouritesFragment mFavFrag;

    public MovieListsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return ListTopRatedFragment.newInstance();
            case 1:
                return ListPopularFragment.newInstance();
            case 2:
                return ListFavouritesFragment.newInstance();
            default:
                return null;
        }
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

    //Based on Tony Chans answer on https://stackoverflow.com/questions/14035090/how-to-get-existing-fragments-when-using-fragmentpageradapter
    @SuppressWarnings("NullableProblems")
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFrag = (Fragment) super.instantiateItem(container, position);

        switch (position){
            case 0:
                mTopFrag = (ListTopRatedFragment) createdFrag;
                break;
            case 1:
                mPopFrag = (ListPopularFragment) createdFrag;
                break;
            case 2:
                mFavFrag = (ListFavouritesFragment) createdFrag;
                break;
            default:
                break;
        }
        return createdFrag;
    }

    public ListTopRatedFragment getTopFrag(){ return mTopFrag; }

    public ListPopularFragment getPopFrag() { return mPopFrag; }

    public ListFavouritesFragment getFavFrag() { return mFavFrag; }

}
