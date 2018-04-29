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
import android.support.v4.content.res.ResourcesCompat;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import javax.annotation.Nullable;

import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;

public class AnimationHelper {

    private final Context mContext;
    private final Movie mMovie;

    private ValueAnimator mVoteTextAnim;
    private ObjectAnimator mVoteProgressAnim;
    private AnimatedVectorDrawableCompat mFabAnim;
    private ValueAnimator mOverviewAnim;

    private int mFavouriteColor;
    private int mDefaultColor;

    private ImageView mIvFabAnimation;
    private FloatingActionButton mFab;


    public AnimationHelper(Context context, Movie movie,
                           ImageView fabAnimation, FloatingActionButton fab){

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
                                          @NonNull Boolean isFavourite,
                                          @Nullable Integer duration,
                                          @Nullable Interpolator interpolator,
                                          @NonNull InitialAnimationUpdateListener listener){

        mVoteTextAnim = ValueAnimator.ofFloat(0.0f, mMovie.getVoteAverage().floatValue())
                .setDuration(duration != null ? duration : 2000);

        mVoteTextAnim.setInterpolator(interpolator != null ?
                interpolator : new BounceInterpolator());

        mVoteTextAnim.addUpdateListener(animation -> listener.updatedValue(
                animation.getAnimatedValue().toString().substring(0, 3)));

        mVoteProgressAnim = ObjectAnimator.ofInt(progressBar, "progress",
                ((int) Math.round(mMovie.getVoteAverage() * 10)))
                .setDuration(duration != null ? duration : 2000);

        mVoteProgressAnim.setInterpolator(interpolator != null ?
                interpolator : new BounceInterpolator());

        mVoteTextAnim.start();
        mVoteProgressAnim.start();

        fabAnim(true, isFavourite);

    }

    private void fabAnim(boolean init, boolean favCheck){

        if (init){
            mFabAnim = AnimatedVectorDrawableCompat.create(mContext, favCheck ?
                    R.drawable.ic_anim_heart_enter_to_fav :
                    R.drawable.ic_anim_heart_enter_to_default);
        } else {
            mFabAnim = AnimatedVectorDrawableCompat.create(mContext, favCheck ?
                    R.drawable.ic_anim_heart_to_fav : R.drawable.ic_anim_heart_from_fav);
        }

        mIvFabAnimation.setImageDrawable(mFabAnim);

        mFabAnim.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                mFab.setBackgroundTintList(ColorStateList.valueOf(favCheck ?
                        mFavouriteColor : mDefaultColor));
                MovieUtils.toggleViewVisibility(mIvFabAnimation, mFab);
                super.onAnimationEnd(drawable);
            }
        });

        MovieUtils.toggleViewVisibility(mIvFabAnimation, mFab);
        mFabAnim.start();

    }

    public void runFabAnim(boolean isFavourite){
        fabAnim(false, isFavourite);
    }

    public void runOverviewAnim(int offsetValue, OverviewAnimationUpdateListener listener){
        mOverviewAnim = ValueAnimator.ofInt(0, -offsetValue).setDuration(offsetValue);

        mOverviewAnim.setInterpolator(new DecelerateInterpolator());
        mOverviewAnim.addUpdateListener(animation ->
                listener.updatedValue((int)animation.getAnimatedValue()));

        mOverviewAnim.start();

    }

    public void disposeAnimations(){
        if (mVoteProgressAnim != null && mVoteProgressAnim.isStarted()) mVoteProgressAnim.cancel();
        if (mVoteTextAnim != null && mVoteTextAnim.isStarted()) mVoteTextAnim.cancel();
        if (mFabAnim != null && mFabAnim.isRunning()) mFabAnim.stop();
        if (mOverviewAnim != null && mOverviewAnim.isRunning()) mOverviewAnim.cancel();
    }

    public interface InitialAnimationUpdateListener{
        void updatedValue(String updatedValue);
    }

    public interface OverviewAnimationUpdateListener{
        void updatedValue(int value);
    }

}
