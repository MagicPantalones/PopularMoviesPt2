package io.magics.popularmovies;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.ViewUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.fragments.MovieDetailsOverview;
import io.magics.popularmovies.fragments.MovieDetailsTrailers;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.models.Reviews;
import io.magics.popularmovies.models.Trailers;
import io.magics.popularmovies.networkutils.ApiUtils;
import io.magics.popularmovies.utils.GlideApp;

import static io.magics.popularmovies.utils.MovieUtils.*;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MovieDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieDetailsFragment extends DialogFragment
    implements MovieListsActivity.DetailsListeners{

    public static final String ARG_MOVIE = "movie";
    public static final String ARG_IS_FAVOURITE = "isFavourite";

    private Movie mMovie;
    private boolean mIsFavourite;
    private Trailers mTrailers;
    private Reviews mReviews;

    @BindView(R.id.iv_poster_detail) ImageView mPoster;
    @BindView(R.id.tv_movie_title_detail) TextView mTitle;
    @BindView(R.id.tv_release_date_detail) TextView mRealeaseDate;
    @BindView(R.id.pb_vote_count_detail) ProgressBar mVoteBar;
    @BindView(R.id.tv_vote_average_detail) TextView mVoteNumber;
    @BindView(R.id.fav_fab) FloatingActionButton mFavFab;
    @BindView(R.id.fav_fab_anim) ImageView mFavFabAnim;
    @BindView(R.id.bottom_nav_details) BottomNavigationView mBotNav;
    @BindView(R.id.nsv_contentNsv) NestedScrollView mNestedScrollView;

    private Unbinder mUnbinder;
    private ValueAnimator mVoteTextAnim;
    private ObjectAnimator mVoteProgressAnim;
    private AnimatedVectorDrawableCompat mFabAnim;

    int tempColor1;
    int tempColor2;

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

        FragmentManager fragManager = getChildFragmentManager();
        Context context = root.getContext();

        ImageSize imageSize = getOptimalImgSize(context);
        String posterUrl = ApiUtils.posterUrlConverter(imageSize, mMovie.getPosterUrl());
        Long voteCalcLong = Math.round(mMovie.getVoteAverage() * 10);

        mVoteTextAnim = ValueAnimator.ofFloat(0.0f, mMovie.getVoteAverage().floatValue());
        mVoteProgressAnim = ObjectAnimator.ofInt(mVoteBar, "progress", voteCalcLong.intValue());

        tempColor1 = ResourcesCompat.getColor(context.getResources(),
                R.color.colorSecondaryAccent,
                context.getTheme());
        tempColor2 = ResourcesCompat.getColor(
                context.getResources(),
                R.color.colorPrimaryDark,
                context.getTheme());

        mFavFab.setBackgroundTintList(ColorStateList.valueOf(mIsFavourite ? tempColor1 : tempColor2));

        mFavFab.setOnClickListener(v -> {
            if (mIsFavourite) ((MovieListsActivity)context).deleteFromFavourites(mMovie);
            else if (!mIsFavourite) ((MovieListsActivity)context).addToFavourites(mMovie);
            mIsFavourite = !mIsFavourite;
            fabAnim();
        });

        mTitle.setText(mMovie.getTitle());
        mRealeaseDate.setText(formatDate(mMovie.getReleaseDate()));

        mVoteTextAnim.setDuration(2000);
        mVoteTextAnim.setInterpolator(new BounceInterpolator());
        mVoteTextAnim.addUpdateListener(valueAnimator -> {
            String shownVal = valueAnimator.getAnimatedValue().toString();
            shownVal = shownVal.substring(0, 3);
            mVoteNumber.setText(shownVal);
        });
        mVoteTextAnim.start();

        mVoteProgressAnim.setDuration(2000);
        mVoteProgressAnim.setInterpolator(new BounceInterpolator());
        mVoteProgressAnim.start();

        GlideApp.with(mPoster)
                .load(posterUrl)
                .centerCrop()
                .placeholder(R.drawable.bg_loading_realydarkgrey)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(mPoster);

        mBotNav.setOnNavigationItemSelectedListener(item -> {
            Fragment frag = null;
            switch (item.getItemId()){
                case R.id.action_overview:
                    frag = MovieDetailsOverview.newInstance(mMovie);
                    break;
                case R.id.action_trailers:
                    frag = MovieDetailsTrailers.newInstance(mTrailers);
                    break;
            }
            FragmentTransaction ft = fragManager.beginTransaction();
            ft.replace(R.id.frame_trailers_and_reviews, frag);
            ft.commit();
            return true;
        });

        FragmentTransaction ft = fragManager.beginTransaction();
        ft.add(R.id.frame_trailers_and_reviews, MovieDetailsOverview.newInstance(mMovie));
        ft.commit();

        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MovieListsActivity) context).registerDetailListeners(this);
    }

    @Override
    public void onDestroy() {
        if (mVoteProgressAnim != null && mVoteProgressAnim.isStarted()) mVoteProgressAnim.cancel();
        if (mVoteTextAnim != null && mVoteTextAnim.isStarted()) mVoteTextAnim.cancel();
        if (mFabAnim != null && mFabAnim.isRunning()) mFabAnim.stop();
        mUnbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        ((MovieListsActivity) getContext()).unRegisterDetailListeners();
        super.onDetach();
    }

    private int dpToPx(int dp){
        float scale = getResources().getDisplayMetrics().density;
        return Math.round(scale * dp);
    }

    private void fabAnim(){

        mFabAnim = AnimatedVectorDrawableCompat.create(getContext(),
                mIsFavourite ? R.drawable.ic_anim_heart_to_fav : R.drawable.ic_anim_heart_from_fav);
        mFavFabAnim.setImageDrawable(mFabAnim);

        mFabAnim.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                mFavFab.setBackgroundTintList(ColorStateList.valueOf(mIsFavourite ? tempColor1 : tempColor2));
                mFavFab.setVisibility(View.VISIBLE);
                super.onAnimationEnd(drawable);
            }
        });

        mFavFab.setVisibility(View.INVISIBLE);
        mFavFabAnim.setVisibility(View.VISIBLE);
        mFabAnim.start();

    }

    @Override
    public void onTrailerFetchFinished(Trailers trailers) {
        mTrailers = trailers;
    }

    @Override
    public void onReviewFetchFinished(Reviews reviews) {
        mReviews = reviews;
    }
}
