package io.magics.popularmovies.fragments.detailfragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.transition.Slide;
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
import io.magics.popularmovies.utils.GlideApp;
import io.magics.popularmovies.utils.MovieUtils;

import static io.magics.popularmovies.utils.AnimationHelper.*;
import static io.magics.popularmovies.utils.MovieUtils.*;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MovieDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieDetailsFragment extends Fragment {

    public static final String ARG_MOVIE = "movie";
    public static final String ARG_IS_FAVOURITE = "isFavourite";

    private static final String OVERVIEW_TAG = "overviewTag";
    private static final String TRAILER_TAG = "trailerTag";
    private static final String REVIEW_TAG = "reviewTag";

    private static final int CARD_FRAGMENT_CONTAINER = R.id.detail_fragment_container;

    private Movie mMovie;
    private boolean mIsFavourite;
    private int mCurrentOptionsId;

    @BindView(R.id.detail_fragment_poster)
    ImageView mPoster;
    @BindView(R.id.wrapper_card_details_poster)
    CardView mPosterWrapperCard;
    @BindView(R.id.wrapper_details_main_card)
    CardView mMainCardWrapper;
    @BindView(R.id.tv_details_title)
    TextView mTitle;
    @BindView(R.id.tv_details_release)
    TextView mReleaseDate;
    @BindView(R.id.pb_details_vote)
    ProgressBar mVoteBar;
    @BindView(R.id.tv_details_vote)
    TextView mVoteNumber;
    @BindView(R.id.fav_fab)
    FloatingActionButton mFavFab;
    @BindView(R.id.fav_fab_anim)
    ImageView mFavFabAnim;
    @BindView(R.id.detail_fragment_container)
    FrameLayout mDetailsContainer;
    @BindView(R.id.nav_detail_frag)
    NavigationTabBar mNavTabBar;


    private Unbinder mUnbinder;
    private FragmentManager mFragManager;
    private DetailFragInteractionHandler mFragInteractionHandler;

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
        mFragManager = getChildFragmentManager();

        if (savedInstanceState == null){
            mCurrentOptionsId = -1;
        }

        if (mFragManager.getFragments() == null || mFragManager.getFragments().isEmpty()) {
            MovieDetailsOverview overviewFrag = MovieDetailsOverview.newInstance(mMovie);
            MovieDetailsTrailers trailerFrag = new MovieDetailsTrailers();
            MovieDetailsReviews reviewFrag = new MovieDetailsReviews();

            mFragManager.beginTransaction()
                    .add(CARD_FRAGMENT_CONTAINER, overviewFrag, OVERVIEW_TAG)
                    .add(CARD_FRAGMENT_CONTAINER, trailerFrag, TRAILER_TAG)
                    .add(CARD_FRAGMENT_CONTAINER, reviewFrag, REVIEW_TAG)
                    .hide(overviewFrag)
                    .hide(trailerFrag)
                    .hide(reviewFrag)
                    .commit();
        }


        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = view.getContext();

        mMainCardWrapper.setTransitionName("wrapper" + mMovie.getMovieId().toString());
        mPosterWrapperCard.setTransitionName(mMovie.getMovieId().toString());

        //noinspection ConstantConditions
        mAnimator = new AnimationHelper(getContext(), mMovie, mFavFabAnim, mFavFab);

        mAnimator.runInitialDetailAnimation(mVoteBar, mIsFavourite, null, null,
                updatedValue -> mVoteNumber.setText(updatedValue));

        mTitle.setText(mMovie.getTitle());
        mReleaseDate.setText(formatDate(mMovie.getReleaseDate()));



        GlideApp.with(this)
                .load(posterUrlConverter(getOptimalImgSize(context), mMovie.getPosterUrl()))
                .centerCrop()
                .placeholder(R.drawable.bg_loading_realydarkgrey)
                .dontAnimate()
                .into(mPoster);

        mFavFab.setOnClickListener(v -> {
            mFragInteractionHandler.favFabClicked(mMovie, mIsFavourite);
            mIsFavourite = !mIsFavourite;
            mAnimator.runFabAnim(mIsFavourite);
        });

        int defaultColor = ResourcesCompat.getColor(getResources(), R.color.colorPrimary,
                context.getTheme());

        ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(new NavigationTabBar.Model.Builder(getResDrawable(
                R.drawable.ic_info_outline_black_24dp), defaultColor).build());
        models.add(new NavigationTabBar.Model.Builder(getResDrawable(
                R.drawable.ic_movie_black_24dp), defaultColor).build());
        models.add(new NavigationTabBar.Model.Builder(
                getResDrawable(R.drawable.ic_star_half_black_24dp),defaultColor).build());

        mNavTabBar.setModels(models);

        mNavTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(NavigationTabBar.Model model, int index) {
                //Intentionally Empty
            }

            @Override
            public void onEndTabSelected(NavigationTabBar.Model model, int index) {
                if (index == mCurrentOptionsId){
                    mNavTabBar.deselect();
                    mCurrentOptionsId = -1;
                } else {
                    mCurrentOptionsId = index;
                }
            }
        });

    }

    private Drawable getResDrawable(int resId){
        //noinspection ConstantConditions
        return ResourcesCompat.getDrawable(getResources(), resId, getContext().getTheme());
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
