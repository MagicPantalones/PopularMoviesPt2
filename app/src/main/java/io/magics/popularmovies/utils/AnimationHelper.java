package io.magics.popularmovies.utils;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.transitionseverywhere.Explode;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;
import com.transitionseverywhere.extra.Scale;

import java.util.ArrayList;

import javax.annotation.Nullable;

import io.magics.popularmovies.MovieListsActivity;
import io.magics.popularmovies.R;
import io.magics.popularmovies.fragments.listfragments.ListAdapter;
import io.magics.popularmovies.models.Movie;

public class AnimationHelper {

    private final Context mContext;
    private Movie mMovie;

    private ValueAnimator mVoteTextAnim;
    private ObjectAnimator mVoteProgressAnim;
    private AnimatedVectorDrawableCompat mFabAnim;
    private ValueAnimator mOverviewAnim;

    private int mFavouriteColor;
    private int mDefaultColor;
    private Drawable mPosterDrawable;

    private ImageView mIvFabAnimation;
    private FloatingActionButton mFab;

    public AnimationHelper(Context context){
        this.mContext = context;

        mFavouriteColor = ResourcesCompat.getColor(context.getResources(),
                R.color.colorSecondaryAccent, context.getTheme());
        mDefaultColor = ResourcesCompat.getColor(context.getResources(),
                R.color.colorPrimaryDark, context.getTheme());

    }

    public void prepareToDetailAnimation(RecyclerView recyclerView, View v, Movie movie,
                                         OnExplodeRecyclerAnimationEnd animationEnd){

        mPosterDrawable = ((ImageView) v.findViewById(R.id.iv_poster)).getDrawable();
        ListAdapter adapter = (ListAdapter) recyclerView.getAdapter();
        GridLayoutManager manager = (GridLayoutManager)recyclerView.getLayoutManager();

        int adapterPosition = manager.findFirstVisibleItemPosition();
        int adapterOffset = manager.findViewByPosition(adapterPosition).getTop();

        ((MovieListsActivity) mContext).saveListAdapterData(
                adapter.getMovieData(), adapterPosition, adapterOffset);

        final Rect viewRect = new Rect();
        v.getGlobalVisibleRect(viewRect);

        TransitionSet transitionSet = new TransitionSet()
                .addTransition(new Explode().setEpicenterCallback(new Transition.EpicenterCallback() {
                    @Override
                    public Rect onGetEpicenter(Transition transition) {
                        return viewRect;
                    }
                }).excludeTarget(v, true)).addTransition(new Scale().addTarget(v))
                .addListener(new Transition.TransitionListenerAdapter(){
                    @Override
                    public void onTransitionEnd(Transition transition) {
                        Toast.makeText(mContext, "Animation End", Toast.LENGTH_LONG).show();
                        transition.removeListener(this);
                        animationEnd.onAnimationEnd();
                    }
                });

        TransitionManager.beginDelayedTransition(recyclerView, transitionSet);
        recyclerView.setAdapter(null);

    }

    public void dispose(){
        alwaysDispose();
    }

    private void alwaysDispose(){
        if (mVoteProgressAnim != null && mVoteProgressAnim.isStarted()) mVoteProgressAnim.cancel();
        if (mVoteTextAnim != null && mVoteTextAnim.isStarted()) mVoteTextAnim.cancel();
        if (mFabAnim != null && mFabAnim.isRunning()) mFabAnim.stop();
        if (mOverviewAnim != null && mOverviewAnim.isRunning()) mOverviewAnim.cancel();
    }

    public interface OnExplodeRecyclerAnimationEnd{
        void onAnimationEnd();
    }

    public interface InitialDetailAnimationUpdateListener {
        void updatedValue(String updatedValue);
    }

    public interface OverviewAnimationUpdateListener{
        void updatedValue(int value);
    }

    public class DetailAnimationsHelper {

        public DetailAnimationsHelper(ImageView fabAnimImageView,
                                      FloatingActionButton fab, Movie movie) {
            mMovie = movie;
            mIvFabAnimation = fabAnimImageView;
            mFab = fab;
        }

        public void dispose(){
            alwaysDispose();
            mIvFabAnimation = null;
            mFab = null;
        }

        /**
         * @param interpolator & @param duration are only there for design tests.
         */
        public void runInitialDetailAnimation(@NonNull ProgressBar progressBar,
                                              @NonNull Boolean isFavourite,
                                              @Nullable Integer duration,
                                              @Nullable Interpolator interpolator,
                                              @NonNull InitialDetailAnimationUpdateListener listener){

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


    }

}
