package io.magics.popularmovies.utils;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import javax.annotation.Nullable;

import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;

public class AnimationHelper {

    public static final int FLIP_LEFT = 71;
    public static final int FLIP_RIGHT = 72;

    public static final int SLIDE_ENTER = 81;
    public static final int SLIDE_EXIT = 82;

    private final Context mContext;
    private final Movie mMovie;

    private ValueAnimator mVoteTextAnim;
    private ObjectAnimator mVoteProgressAnim;
    private AnimatedVectorDrawableCompat mFabAnim;

    private int mFavouriteColor;
    private int mDefaultColor;

    private ImageView mIvFabAnimation;
    private FloatingActionButton mFab;


    public AnimationHelper(Context context, Movie movie,
                           ImageView fabAnimation, FloatingActionButton fab) {

        this.mContext = context;
        this.mMovie = movie;

        this.mIvFabAnimation = fabAnimation;
        this.mFab = fab;

        mFavouriteColor = ResourcesCompat.getColor(context.getResources(),
                R.color.colorSecondaryAccent, context.getTheme());
        mDefaultColor = ResourcesCompat.getColor(context.getResources(),
                R.color.colorPrimaryDark, context.getTheme());

    }

    public void runInitialDetailAnimation(@NonNull ProgressBar progressBar,
                                          boolean isFavourite,
                                          @Nullable Integer duration,
                                          @Nullable Interpolator interpolator,
                                          @NonNull InitialAnimationUpdateListener listener) {

        mVoteTextAnim = ValueAnimator.ofFloat(0.0f, mMovie.getVoteAverage().floatValue());

        mVoteTextAnim.setDuration(duration != null ? duration : 2000);
        mVoteTextAnim.setInterpolator(interpolator != null ?
                interpolator : new BounceInterpolator());

        mVoteTextAnim.addUpdateListener(animation -> listener.updatedValue(
                animation.getAnimatedValue().toString().substring(0, 3)));


        mVoteProgressAnim = ObjectAnimator.ofInt(progressBar, "progress",
                ((int) Math.round(mMovie.getVoteAverage() * 10)));

        mVoteProgressAnim.setDuration(duration != null ? duration : 2000);
        mVoteProgressAnim.setInterpolator(interpolator != null ?
                interpolator : new BounceInterpolator());

        mVoteTextAnim.start();
        mVoteProgressAnim.start();

        mFab.setBackgroundTintList(
                ColorStateList.valueOf(isFavourite ? mFavouriteColor : mDefaultColor));

    }

    public void fabAnim(boolean favCheck) {

        mFabAnim = AnimatedVectorDrawableCompat.create(mContext, favCheck ?
                R.drawable.ic_anim_heart_to_fav : R.drawable.ic_anim_heart_from_fav);

        mIvFabAnimation.setImageDrawable(mFabAnim);

        mFabAnim.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                mFab.setBackgroundTintList(ColorStateList.valueOf(favCheck ?
                        mFavouriteColor : mDefaultColor));
                mFab.show();
                super.onAnimationEnd(drawable);
            }
        });

        mFab.hide();
        mIvFabAnimation.setVisibility(View.VISIBLE);
        mFabAnim.start();

    }

    public void disposeAnimations() {
        if (mVoteProgressAnim != null && mVoteProgressAnim.isStarted()) mVoteProgressAnim.cancel();
        if (mVoteTextAnim != null && mVoteTextAnim.isStarted()) mVoteTextAnim.cancel();
        if (mFabAnim != null && mFabAnim.isRunning()) mFabAnim.stop();
    }

    public interface InitialAnimationUpdateListener {
        void updatedValue(String updatedValue);
    }

}
