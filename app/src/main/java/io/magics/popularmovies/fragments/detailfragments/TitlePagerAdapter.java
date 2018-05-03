package io.magics.popularmovies.fragments.detailfragments;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.magics.popularmovies.R;


//Got help for this class from: https://www.bignerdranch.com/blog/viewpager-without-fragments/
public class TitlePagerAdapter extends PagerAdapter {

    private Context mContext;


    TitlePagerAdapter(Context context){
        mContext = context;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        TitlePagerEnum titlePage = TitlePagerEnum.values()[position];

        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(titlePage.getLayoutRes(), container, false);

        TextView titleTv = layout.findViewById(R.id.tv_pager_title);
        titleTv.setText(mContext.getText(titlePage.getTitleTextRes()));

        container.addView(layout);

        return layout;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return TitlePagerEnum.values().length;
    }

    public enum TitlePagerEnum {
        POSTER(R.string.nav_poster_title, R.layout.fragment_detail_view_pager_title),
        OVERVIEW(R.string.nav_overview_title, R.layout.fragment_detail_view_pager_title),
        TRAILERS(R.string.nav_trailers_title, R.layout.fragment_detail_view_pager_title),
        REVIEWS(R.string.nav_reviews_title, R.layout.fragment_detail_view_pager_title);

        private int mTitleTextRes;
        private int mLayoutRes;

        TitlePagerEnum(int titleTextRes, int layoutRes) {
            mTitleTextRes = titleTextRes;
            mLayoutRes = layoutRes;
        }

        public int getTitleTextRes() { return mTitleTextRes; }

        public int getLayoutRes() { return mLayoutRes; }

    }
}
