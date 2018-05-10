package io.magics.popularmovies.fragments.detailfragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.AnimationHelper;



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

        mPagerAdapter = new MovieDetailsPagerAdapter(mMovie,
                mTransitionName + mMovie.getPosterUrl(), getChildFragmentManager());

        setSharedElementEnterTransition(TransitionInflater.from(getContext())
                .inflateTransition(R.transition.card_enter_transition));

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                Fragment posterFrag = (Fragment) mPagerAdapter.instantiateItem(mNestedViewPager, 0);

                View view = posterFrag.getView();

                if (view == null){
                    return;
                }

                sharedElements.put(names.get(0), view.findViewById(R.id.nested_poster_wrapper));
            }
        });

        if (savedInstanceState == null) {
            postponeEnterTransition();
        }

        return root;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mNestedViewPager.setOffscreenPageLimit(4);
        mNestedViewPager.setAdapter(mPagerAdapter);


        mTitlesIndicator.setupWithViewPager(mNestedViewPager, true);

        mBtnToolbarBack.setOnClickListener(v -> getActivity().onBackPressed());

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

    public interface DetailFragInteractionHandler {
        void favFabClicked(Movie movie, Boolean isFavourite);
    }

}
