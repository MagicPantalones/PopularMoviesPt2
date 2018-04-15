package io.magics.popularmovies.fragments.detailfragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
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
public class MovieDetailsFragment extends DialogFragment {

    public static final String ARG_MOVIE = "movie";
    public static final String ARG_IS_FAVOURITE = "isFavourite";

    private Movie mMovie;
    private boolean mIsFavourite;

    @BindView(R.id.iv_poster_detail) ImageView mPoster;
    @BindView(R.id.tv_movie_title_detail) TextView mTitle;
    @BindView(R.id.tv_release_date_detail) TextView mReleaseDate;
    @BindView(R.id.pb_vote_count_detail) ProgressBar mVoteBar;
    @BindView(R.id.tv_vote_average_detail) TextView mVoteNumber;
    @BindView(R.id.fav_fab) FloatingActionButton mFavFab;
    @BindView(R.id.fav_fab_anim) ImageView mFavFabAnim;
    @BindView(R.id.bottom_nav_details) BottomNavigationView mBotNav;

    private Unbinder mUnbinder;
    private FragmentManager mFragManager;
    private DetailFragInteractionHandler mFragInteractionHandler;

    private ValueAnimator mVoteTextAnim;
    private ObjectAnimator mVoteProgressAnim;
    private AnimatedVectorDrawableCompat mFabAnim;

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
            ft.add(R.id.frame_trailers_and_reviews, MovieDetailsOverview.newInstance(mMovie));
            ft.commit();
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
        mFavFab.setBackgroundTintList(ColorStateList.valueOf(mIsFavourite ? mFavouriteColor : mDefaultColor));

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
            fabClickAnim();
        });

        mBotNav.setOnNavigationItemSelectedListener(item -> {
            Fragment frag = null;
            switch (item.getItemId()){
                case R.id.action_overview:
                    frag = MovieDetailsOverview.newInstance(mMovie);
                    break;
                case R.id.action_trailers:
                    frag = new MovieDetailsTrailers();
                    break;
                case R.id.action_reviews:
                    frag = new MovieDetailsReviews();
                    break;
                default:
                    break;
            }
            FragmentTransaction ft = mFragManager.beginTransaction();
            ft.replace(R.id.frame_trailers_and_reviews, frag);
            ft.commit();
            return true;
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DetailFragInteractionHandler) {
            mFragInteractionHandler = (DetailFragInteractionHandler) context;
        }
        else throw new IllegalArgumentException("Not a FavFabClickListener");
    }

    @Override
    public void onDestroyView() {
        if (mVoteProgressAnim != null && mVoteProgressAnim.isStarted()) mVoteProgressAnim.cancel();
        if (mVoteTextAnim != null && mVoteTextAnim.isStarted()) mVoteTextAnim.cancel();
        if (mFabAnim != null && mFabAnim.isRunning()) mFabAnim.stop();
        mUnbinder.unbind();
        super.onDestroyView();
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

    private void fabClickAnim(){

        mFabAnim = AnimatedVectorDrawableCompat.create(Objects.requireNonNull(getContext()),
                mIsFavourite ? R.drawable.ic_anim_heart_to_fav : R.drawable.ic_anim_heart_from_fav);
        mFavFabAnim.setImageDrawable(mFabAnim);

        mFabAnim.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                mFavFab.setBackgroundTintList(ColorStateList.valueOf(mIsFavourite ? mFavouriteColor : mDefaultColor));
                mFavFab.setVisibility(View.VISIBLE);
                super.onAnimationEnd(drawable);
            }
        });

        mFavFab.setVisibility(View.INVISIBLE);
        mFavFabAnim.setVisibility(View.VISIBLE);
        mFabAnim.start();

    }

    public interface DetailFragInteractionHandler {
        void favFabClicked(Movie movie, Boolean isFavourite);
    }

}
