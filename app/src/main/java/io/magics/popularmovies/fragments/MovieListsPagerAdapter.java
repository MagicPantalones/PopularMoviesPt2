package io.magics.popularmovies.fragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import io.magics.popularmovies.fragments.listfragments.ListFragment;


public class MovieListsPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 3;
    private String[] tabTitles = new String[] {"Top Rated", "Popular", "Favourites"};

    private ListFragment mTopFrag;
    private ListFragment mPopFrag;
    private ListFragment mFavFrag;

    public MovieListsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return ListFragment.newInstance(ListFragment.TOP_FRAGMENT);
            case 1:
                return ListFragment.newInstance(ListFragment.POP_FRAGMENT);
            case 2:
                return ListFragment.newInstance(ListFragment.FAV_FRAGMENT);
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
        ListFragment createdFrag = (ListFragment) super.instantiateItem(container, position);

        switch (position){
            case 0:
                mTopFrag = createdFrag;
                break;
            case 1:
                mPopFrag = createdFrag;
                break;
            case 2:
                mFavFrag = createdFrag;
                break;
            default:
                break;
        }
        return createdFrag;
    }

    public ListFragment getOneListFragment(int fragType){
        switch (fragType){
            case ListFragment.TOP_FRAGMENT:
                return mTopFrag;
            case ListFragment.POP_FRAGMENT:
                return mPopFrag;
            case ListFragment.FAV_FRAGMENT:
                return mFavFrag;
            default:
                //Should never happen.
                throw new IllegalArgumentException("Wrong Fragment Type");
        }
    }

}
