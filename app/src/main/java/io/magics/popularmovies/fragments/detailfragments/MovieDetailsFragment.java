package io.magics.popularmovies.fragments.detailfragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;
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

    private static final String OVERVIEW_FRAG = "overviewFrag";
    private static final String TRAILER_FRAG = "trailerFrag";
    private static final String REVIEW_FRAG = "reviewFrag";

    private Movie mMovie;
    private boolean mIsFavourite;

    @BindView(R.id.detail_fragment) CoordinatorLayout mCoordinator;
    @BindView(R.id.detail_wrapper) ConstraintLayout mDetailWrapper;
    @BindView(R.id.frame_trailers_and_reviews) FrameLayout mFragFrame;
    @BindView(R.id.iv_poster_detail) ImageView mPoster;
    @BindView(R.id.tv_movie_title_detail) TextView mTitle;
    @BindView(R.id.tv_release_date_detail) TextView mReleaseDate;
    @BindView(R.id.pb_vote_count_detail) ProgressBar mVoteBar;
    @BindView(R.id.tv_vote_average_detail) TextView mVoteNumber;
    @BindView(R.id.fav_fab) FloatingActionButton mFavFab;
    @BindView(R.id.fav_fab_anim) ImageView mFavFabAnim;
    @BindView(R.id.bottom_nav_details) BottomNavigationView mBotNav;
    @BindView(R.id.app_bar_main_details) AppBarLayout mAppBar;
    @BindView(R.id.nsv_contentNsv) NestedScrollView mScrollView;

    private Unbinder mUnbinder;
    private FragmentManager mFragManager;
    private DetailFragInteractionHandler mFragInteractionHandler;

    private ValueAnimator mVoteTextAnim;
    private ObjectAnimator mVoteProgressAnim;
    private AnimatedVectorDrawableCompat mFabAnim;
    private ValueAnimator mOverviewAnim;

    ImageSize mImageSize;
    int mFavouriteColor;
    int mDefaultColor;

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

        mImageSize = getOptimalImgSize(context);

        mFavouriteColor = ResourcesCompat.getColor(
                context.getResources(),
                R.color.colorSecondaryAccent,
                context.getTheme());

        mDefaultColor = ResourcesCompat.getColor(
                context.getResources(),
                R.color.colorPrimaryDark,
                context.getTheme());

        initialAnim();
        fabAnim(true);

        mTitle.setText(mMovie.getTitle());
        mReleaseDate.setText(formatDate(mMovie.getReleaseDate()));

        GlideApp.with(mPoster)
                .load(posterUrlConverter(mImageSize, mMovie.getPosterUrl()))
                .centerCrop()
                .placeholder(R.drawable.bg_loading_realydarkgrey)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(mPoster);

        mFavFab.setOnClickListener(v -> {
            mFragInteractionHandler.favFabClicked(mMovie, mIsFavourite);
            mIsFavourite = !mIsFavourite;
            fabAnim(false);
        });

        mBotNav.setOnNavigationItemSelectedListener(item -> {
            Fragment frag = null;

            if (!item.isChecked()) {
                switch (item.getItemId()) {
                    case R.id.action_overview:
                        frag = MovieDetailsOverview.newInstance(mMovie);
                        break;
                    case R.id.action_trailers:
                        frag = new MovieDetailsTrailers();
                        mAppBar.setExpanded(true, true);
                        mScrollView.setNestedScrollingEnabled(false);
                        break;
                    case R.id.action_reviews:
                        frag = new MovieDetailsReviews();
                        mScrollView.setNestedScrollingEnabled(true);
                        break;
                    default:
                        break;
                }

                FragmentTransaction ft = mFragManager.beginTransaction();
                ft.replace(R.id.frame_trailers_and_reviews, frag);
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
        if (mVoteProgressAnim != null && mVoteProgressAnim.isStarted()) mVoteProgressAnim.cancel();
        if (mVoteTextAnim != null && mVoteTextAnim.isStarted()) mVoteTextAnim.cancel();
        if (mFabAnim != null && mFabAnim.isRunning()) mFabAnim.stop();
        if (mOverviewAnim != null && mOverviewAnim.isRunning()) mOverviewAnim.cancel();
        mUnbinder.unbind();
        mFragInteractionHandler.onFragmentExit();
        super.onDestroyView();
    }

    private void setAppBarOffsetForOverview(int totalTextHeight, int offset){
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mFragFrame.getLayoutParams();
        int margin = lp.topMargin * 2;
        int heightLimit = mScrollView.getTop();
        int visibleText = totalTextHeight + mDetailWrapper.getHeight();

        if (visibleText > heightLimit) {
            AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams)
                    mAppBar.getLayoutParams()).getBehavior();
            if (behavior != null) {
                behavior.setTopAndBottomOffset(0);
                mOverviewAnim = ValueAnimator.ofInt();
                mOverviewAnim.setInterpolator(new DecelerateInterpolator());
                mOverviewAnim.addUpdateListener(animation -> {
                    behavior.setTopAndBottomOffset((int) animation.getAnimatedValue());
                    mAppBar.requestLayout();
                });
                mOverviewAnim.setIntValues(0, -(margin / 2 + offset));
                mOverviewAnim.setDuration(offset);
                mOverviewAnim.start();
                //behavior.onNestedPreScroll(mCoordinator, mAppBar, null, 0, offset, new int[]{0, 0}, ViewCompat.TYPE_NON_TOUCH);
            }
        }
        mScrollView.setNestedScrollingEnabled(false);
    }

    private void initialAnim(){

        mVoteTextAnim = ValueAnimator.ofFloat(0.0f, mMovie.getVoteAverage().floatValue());

        mVoteTextAnim.setDuration(2000);
        mVoteTextAnim.setInterpolator(new BounceInterpolator());

        mVoteTextAnim.addUpdateListener(valueAnimator -> {
            String shownVal = valueAnimator.getAnimatedValue().toString();
            shownVal = shownVal.substring(0, 3);
            mVoteNumber.setText(shownVal);
        });

        mVoteTextAnim.start();

        mVoteProgressAnim = ObjectAnimator.ofInt(mVoteBar, "progress",
                ((Long)Math.round(mMovie.getVoteAverage() * 10)).intValue());

        mVoteProgressAnim.setDuration(2000);
        mVoteProgressAnim.setInterpolator(new BounceInterpolator());

        mVoteProgressAnim.start();
    }

    private void fabAnim(boolean enterAnim){

        if (enterAnim){
            mFabAnim = AnimatedVectorDrawableCompat.create(Objects.requireNonNull(getContext()),
                    mIsFavourite ? R.drawable.ic_anim_heart_enter_to_fav : R.drawable.ic_anim_heart_enter_to_default);
        } else {
            mFabAnim = AnimatedVectorDrawableCompat.create(Objects.requireNonNull(getContext()),
                    mIsFavourite ? R.drawable.ic_anim_heart_to_fav : R.drawable.ic_anim_heart_from_fav);
        }

        mFavFabAnim.setImageDrawable(mFabAnim);

        mFabAnim.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                mFavFab.setBackgroundTintList(ColorStateList.valueOf(mIsFavourite ? mFavouriteColor : mDefaultColor));
                mFavFab.setVisibility(View.VISIBLE);
                mFavFabAnim.setVisibility(View.INVISIBLE);
                super.onAnimationEnd(drawable);
            }
        });

        mFavFab.setVisibility(View.INVISIBLE);
        mFavFabAnim.setVisibility(View.VISIBLE);
        mFabAnim.start();

    }

    @Override
    public void onFragmentDrawn(int totalTextHeight) {
        setAppBarOffsetForOverview(totalTextHeight, mBotNav.getHeight() / 2);
    }

    public interface DetailFragInteractionHandler {
        void favFabClicked(Movie movie, Boolean isFavourite);
        void onFragmentExit();
    }

}
