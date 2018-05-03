package io.magics.popularmovies.fragments.detailfragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import devlight.io.library.ntb.NavigationTabBar;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.AnimationHelper;
import io.magics.popularmovies.utils.MovieUtils;

import static io.magics.popularmovies.utils.MovieUtils.*;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MovieDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieDetailsFragment extends Fragment {

    public static final String ARG_MOVIE = "movie";
    public static final String ARG_IS_FAVOURITE = "isFavourite";

    private Movie mMovie;
    private boolean mIsFavourite;

    @BindView(R.id.wrapper_details_main_card)
    CardView mMainCardWrapper;
    @BindView(R.id.pb_details_vote)
    ProgressBar mVoteBar;
    @BindView(R.id.tv_details_vote)
    TextView mVoteNumber;
    @BindView(R.id.fav_fab)
    FloatingActionButton mFavFab;
    @BindView(R.id.fav_fab_anim)
    ImageView mFavFabAnim;
    @BindView(R.id.nested_details_container)
    ViewPager mNestedViewPager;
    @BindView(R.id.nested_details_titles)
    ViewPager mNestedPagerTitles;


    private Unbinder mUnbinder;
    private DetailFragInteractionHandler mFragInteractionHandler;

    private MovieDetailsPagerAdapter mPagerAdapter;
    private TitlePagerAdapter mPagerTitleAdapter;
    private AnimationHelper mAnimator;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    public static MovieDetailsFragment newInstance(Movie movie, boolean isFavourite) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        args.putBoolean(ARG_IS_FAVOURITE, isFavourite);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        postponeEnterTransition();

        if (getArguments() != null) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
            mIsFavourite = getArguments().getBoolean(ARG_IS_FAVOURITE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail_movie, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        mPagerAdapter = new MovieDetailsPagerAdapter(mMovie, getChildFragmentManager());
        mPagerTitleAdapter = new TitlePagerAdapter(getContext());

        return root;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mNestedViewPager.setOffscreenPageLimit(4);
        mNestedViewPager.setAdapter(mPagerAdapter);

        mNestedPagerTitles.setOffscreenPageLimit(4);
        mNestedPagerTitles.setAdapter(mPagerTitleAdapter);

        mNestedViewPager.addOnPageChangeListener(new OnViewPagerPageChange(mNestedViewPager, mNestedPagerTitles));
        mNestedPagerTitles.addOnPageChangeListener(new OnViewPagerPageChange(mNestedPagerTitles, mNestedViewPager));

        mMainCardWrapper.setTransitionName("wrapper" + mMovie.getMovieId().toString());

        //noinspection ConstantConditions
        mAnimator = new AnimationHelper(getContext(), mMovie, mFavFabAnim, mFavFab);

        mAnimator.runInitialDetailAnimation(mVoteBar, mIsFavourite, null, null,
                updatedValue -> mVoteNumber.setText(updatedValue));

        mFavFab.setOnClickListener(v -> {
            mFragInteractionHandler.favFabClicked(mMovie, mIsFavourite);
            mIsFavourite = !mIsFavourite;
            mAnimator.runFabAnim(mIsFavourite);
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DetailFragInteractionHandler) {
            mFragInteractionHandler = (DetailFragInteractionHandler) context;
        }

    }

    @Override
    public void onDestroyView() {
        mAnimator.disposeAnimations();
        mUnbinder.unbind();
        super.onDestroyView();
    }

    public interface DetailFragInteractionHandler {
        void favFabClicked(Movie movie, Boolean isFavourite);
    }

    public class OnViewPagerPageChange implements ViewPager.OnPageChangeListener {

        private ViewPager mMasterPager;
        private ViewPager mSlavePager;
        private int mScrollState = ViewPager.SCROLL_STATE_IDLE;

        OnViewPagerPageChange(ViewPager master, ViewPager slave){
            mMasterPager = master;
            mSlavePager = slave;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) return;
            mSlavePager.scrollTo(mMasterPager.getScrollX()
                    * mSlavePager.getWidth() / mMasterPager.getWidth(), 0);
        }

        @Override
        public void onPageSelected(int position) {
            //Empty
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;
            if (state == ViewPager.SCROLL_STATE_IDLE){
                mSlavePager.setCurrentItem(mMasterPager.getCurrentItem(), false);
            }
        }
    }
}
