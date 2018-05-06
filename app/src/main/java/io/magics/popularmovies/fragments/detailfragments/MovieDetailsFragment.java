package io.magics.popularmovies.fragments.detailfragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
    public static final String ARG_TRANSITION_NAME = "transitionName";

    private Movie mMovie;
    private boolean mIsFavourite;
    private String mTransitionName;

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
    @BindView(R.id.btn_detail_bar_back)
    ImageView mBtnToolbarBack;
    @BindView(R.id.titles_indicator)
    TabLayout mTitlesIndicator;
    @BindView(R.id.wrap_test)
    FrameLayout mWrapTest;


    private Unbinder mUnbinder;
    private DetailFragInteractionHandler mFragInteractionHandler;

    private MovieDetailsPagerAdapter mPagerAdapter;
    private AnimationHelper mAnimator;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    public static MovieDetailsFragment newInstance(Movie movie, boolean isFavourite,
                                                   String transitionName) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        args.putBoolean(ARG_IS_FAVOURITE, isFavourite);
        args.putString(ARG_TRANSITION_NAME, transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
            mIsFavourite = getArguments().getBoolean(ARG_IS_FAVOURITE);
            mTransitionName = getArguments().getString(ARG_TRANSITION_NAME);
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail_movie, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        mPagerAdapter = new MovieDetailsPagerAdapter(mMovie, getChildFragmentManager());

        return root;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setTransitionName(mMainCardWrapper, mTransitionName);

        mNestedViewPager.setOffscreenPageLimit(4);
        mNestedViewPager.setAdapter(mPagerAdapter);

        mTitlesIndicator.setupWithViewPager(mNestedViewPager, true);

        mBtnToolbarBack.setOnClickListener(v -> getActivity().onBackPressed());

        mMainCardWrapper.setPreventCornerOverlap(true);

        //noinspection ConstantConditions
        mAnimator = new AnimationHelper(getContext(), mMovie, mFavFabAnim, mFavFab);


        mAnimator.runInitialDetailAnimation(mVoteBar, mIsFavourite, null, null,
                updatedValue -> mVoteNumber.setText(updatedValue));

        mFavFab.setOnClickListener(v -> {
            mFragInteractionHandler.favFabClicked(mMovie, mIsFavourite);
            mIsFavourite = !mIsFavourite;
            mAnimator.fabAnim(mIsFavourite);
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

    public void startSharedElementTransition(){
        startPostponedEnterTransition();

        int cx = mWrapTest.getWidth() / 2;
        int cy = mWrapTest.getHeight() / 2;
        float finalRadius = (float) Math.hypot(cx, cy);

        Animator anim = ViewAnimationUtils.createCircularReveal(mWrapTest,
                cx, cy, 0, finalRadius);

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                anim.removeListener(this);
                mFavFab.show();
            }
        });

        mWrapTest.setVisibility(View.VISIBLE);
        anim.start();
    }

    public interface DetailFragInteractionHandler {
        void favFabClicked(Movie movie, Boolean isFavourite);
    }

}
