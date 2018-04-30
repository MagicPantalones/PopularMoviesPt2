package io.magics.popularmovies.fragments.detailfragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.MovieListsActivity;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.AnimationHelper;
import io.magics.popularmovies.utils.GlideApp;

import static io.magics.popularmovies.utils.MovieUtils.*;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MovieDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieDetailsFragment extends Fragment implements MovieDetailsOverview.OverviewFragEvent{

    public static final String ARG_MOVIE = "movie";
    public static final String ARG_IS_FAVOURITE = "isFavourite";

    private static final String OVERVIEW_TAG = "overviewTag";
    private static final String TRAILER_TAG = "trailerTag";
    private static final String REVIEW_TAG = "reviewTag";

    private Movie mMovie;
    private boolean mIsFavourite;
    private boolean mEnterTransitionStarted = false;

    @BindView(R.id.detail_fragment)
    CoordinatorLayout mCoordinator;
    @BindView(R.id.detail_wrapper)
    ConstraintLayout mDetailWrapper;
    @BindView(R.id.frame_trailers_and_reviews)
    FrameLayout mFragFrame;
    @BindView(R.id.iv_poster_detail)
    ImageView mPoster;
    @BindView(R.id.cv_backdrop)
    CardView mPosterCardView;
    @BindView(R.id.tv_movie_title_detail)
    TextView mTitle;
    @BindView(R.id.tv_release_date_detail)
    TextView mReleaseDate;
    @BindView(R.id.pb_vote_count_detail)
    ProgressBar mVoteBar;
    @BindView(R.id.tv_vote_average_detail)
    TextView mVoteNumber;
    @BindView(R.id.fav_fab)
    FloatingActionButton mFavFab;
    @BindView(R.id.fav_fab_anim)
    ImageView mFavFabAnim;
    @BindView(R.id.bottom_nav_details)
    BottomNavigationView mBotNav;
    @BindView(R.id.app_bar_main_details)
    AppBarLayout mAppBar;
    @BindView(R.id.nsv_contentNsv)
    NestedScrollView mScrollView;

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

        //noinspection ConstantConditions

        if (mFragManager.getFragments() == null || mFragManager.getFragments().isEmpty()) {
            FragmentTransaction ft = mFragManager.beginTransaction();
            MovieDetailsOverview frag = MovieDetailsOverview.newInstance(mMovie);
            ft.add(R.id.frame_trailers_and_reviews, frag);
            ft.commit();
        } else {
            Fragment frag = mFragManager.findFragmentById(R.id.frame_trailers_and_reviews);
            if (frag != null && !(frag instanceof MovieDetailsReviews)) {
                mScrollView.setNestedScrollingEnabled(false);
            }
        }
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = view.getContext();

        mPosterCardView.setTransitionName(mMovie.getMovieId().toString());
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

        mBotNav.setOnNavigationItemSelectedListener(item -> {
            FragmentTransaction ft = mFragManager.beginTransaction();

            if (!item.isChecked()) {
                switch (item.getItemId()) {
                    case R.id.action_overview:
                        ft.replace(R.id.frame_trailers_and_reviews,
                                MovieDetailsOverview.newInstance(mMovie), OVERVIEW_TAG);
                        break;
                    case R.id.action_trailers:
                        ft.replace(R.id.frame_trailers_and_reviews, new MovieDetailsTrailers(),
                                TRAILER_TAG);
                        mAppBar.setExpanded(true, true);
                        mScrollView.setNestedScrollingEnabled(false);
                        break;
                    case R.id.action_reviews:
                        ft.replace(R.id.frame_trailers_and_reviews, new MovieDetailsReviews(),
                                REVIEW_TAG);
                        mScrollView.setNestedScrollingEnabled(true);
                        break;
                    default:
                        break;
                }

                ft.commit();

                return true;
            }
            return true;
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

    private void setAppBarOffsetForOverview(int totalTextHeight, int offset){
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mFragFrame.getLayoutParams();
        int margin = lp.topMargin * 2;
        int heightLimit = mScrollView.getTop() - margin;
        int visibleText = totalTextHeight + mDetailWrapper.getHeight();

        if (visibleText > heightLimit) {
            AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams)
                    mAppBar.getLayoutParams()).getBehavior();
            if (behavior != null) {
                behavior.setTopAndBottomOffset(0);
                mAnimator.runOverviewAnim(margin / 2 + offset, value -> {
                    behavior.setTopAndBottomOffset(value);
                    mAppBar.requestLayout();
                });
            }
        }
        mScrollView.setNestedScrollingEnabled(false);
    }

    @Override
    public void onFragmentDrawn(int totalTextHeight) {
        setAppBarOffsetForOverview(totalTextHeight, mBotNav.getHeight() / 2);
    }

    public interface DetailFragInteractionHandler {
        void favFabClicked(Movie movie, Boolean isFavourite);
    }

}
