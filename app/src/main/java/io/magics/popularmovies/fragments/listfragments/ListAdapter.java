package io.magics.popularmovies.fragments.listfragments;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.popularmovies.MovieListsActivity;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.GlideApp;
import io.magics.popularmovies.utils.MovieUtils;
import io.magics.popularmovies.viewmodels.PopListViewModel;
import io.magics.popularmovies.viewmodels.TopListViewModel;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.PosterViewHolder> {


    private List<Movie> mMovieData = new ArrayList<>();
    private int mPosterWidth;
    private int mPosterHeight;
    private final ListItemEventHandler mClickHandler;
    private ReachedEndHandler mReachedEndHandler;
    private MovieUtils.ImageSize mImageSize;
    private int mDefaultColor;
    private int mSelectedPosition;

    private AtomicBoolean mTransitionStarted;

    private boolean mOffline = false;

    private int mListType;

    public interface ListItemEventHandler {
        void onClick(View holder, Movie movie, int transitionIdentifier, int selectedPosition);
        void onImageLoaded(int adapterPosition);
    }

    //Help from https://medium.com/@ayhamorfali/android-detect-when-the-recyclerview-reaches-the-bottom-43f810430e1e
    interface ReachedEndHandler {
        void endReached();
    }

    ListAdapter(ListItemEventHandler listItemEventHandler) {
        this.mClickHandler = listItemEventHandler;
        mListType = ListFragment.FAV_FRAGMENT;
    }

    ListAdapter(ListItemEventHandler listItemEventHandler, ViewModel viewModel, int listType) {
        this.mClickHandler = listItemEventHandler;

        /*
        Calls a method in the provided ViewModel, that will call a listener for
        MovieListsActivity#mDataProvider to fetch another page from the API,
        when a ViewHolder's position is almost at the end.
         */
        mReachedEndHandler = () -> {
            if (viewModel instanceof TopListViewModel) {
                ((TopListViewModel) viewModel).notifyGetMoreTopPages();
            }
            else if (viewModel instanceof PopListViewModel) {
                ((PopListViewModel) viewModel).notifyGetMorePopPages();
            }
        };

        //Uses the list type to provide an extra identifier for the SharedElement's transition name.
        mListType = listType;
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        mDefaultColor = ResourcesCompat.getColor(context.getResources(), R.color.colorSecondary,
                context.getTheme());
        mImageSize = MovieUtils.getOptimalImgSize(context);
        mTransitionStarted = new AtomicBoolean();

        View v = LayoutInflater.from(context).inflate(R.layout.fragment_list_view_holder, parent,
                false);

        /*
        Measures the parent ViewHolder to decide the poster's size. This done to programmatically
        set the ViewHolder sizes based on the user's device size.

        The majority of movie posters have a apect ratio of 2:3, and the ViewHolder have the same
        padding and margin on both the start and end (2 * 8dp on each side).
        Therefore the padding is multiplied by 4 before the value is subtracted from the ViewHolder's
        Width.

        Then I have the width, which is divided by 2 to get the aspect ratio multiplier.
        The aspect ratio multiplier is multiplied by 3, to get the poster height. And multiplied by
        2 to get the poster width.

        This is so i don't have to create different layout files for different screen sizes.
         */

        v.measure(parent.getMeasuredWidth(), parent.getMeasuredHeight());

        int padding = v.findViewById(R.id.parent_wrapper_list).getPaddingStart() * 4;

        int aspectRatioMultiplier = (v.getMeasuredWidth() - padding) / 2;
        mPosterHeight = aspectRatioMultiplier * 3;
        mPosterWidth = aspectRatioMultiplier * 2;


        return new PosterViewHolder(v);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        String posterUrl;
        Movie mfg = mMovieData.get(position);
        ImageView iv = holder.mIv;
        TextView tvTitle = holder.mTvTitle;
        ProgressBar pbVotes = holder.mPbVoteBar;
        TextView tvVotes = holder.mTvVote;
        ImageView shadow = holder.mShadowLayer;

        posterUrl = MovieUtils.posterUrlConverter(mImageSize, mfg.getPosterUrl());

        if (position == mMovieData.size() - 5 && mReachedEndHandler != null && !mOffline) {
            mReachedEndHandler.endReached();
        }

        tvTitle.setText(mfg.getTitle());
        pbVotes.setProgress(mfg.getVoteAverage().intValue() * 10);
        tvVotes.setText(String.valueOf(mfg.getVoteAverage()));
        iv.setContentDescription(mfg.getTitle());
        shadow.setImageDrawable(holder.mGradientDrawable.mutate());

        holder.setTransitionNames(mfg);

        iv.setMinimumWidth(mPosterWidth);
        iv.setMinimumHeight(mPosterHeight);

        holder.itemView.setOnClickListener(v -> {
            mSelectedPosition = holder.getAdapterPosition();
            mClickHandler.onClick(v, mfg, mListType, mSelectedPosition);
        });


        GlideApp.with(iv)
                .load(posterUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_wifi_strength_alert_outline)
                .onlyRetrieveFromCache(mOffline)
                .timeout(30000)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        //Uses AtomicBoolean mTransitionStarted to avoid calling
                        // #startPostponedEnterTransition() multiple times.
                        if (MovieListsActivity.getSelectedPosition() == position &&
                                !mTransitionStarted.getAndSet(true)) {
                            mClickHandler.onImageLoaded(position);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (MovieListsActivity.getSelectedPosition() == position &&
                                !mTransitionStarted.getAndSet(true)) {
                            mClickHandler.onImageLoaded(position);
                        }
                        return false;
                    }
                })
                .into(new ImageViewTarget<Drawable>(iv) {
                    
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        iv.setImageDrawable(resource.getCurrent());

                        /*
                        Checks if MovieData#ShadowInt has been set in the Movie Object.
                        If it has not been set, it will use the Palette class to try to find the
                        most vibrant color in the poster's bitmap.
                        If that is not found, it will try to get a dark vibrant color, then the
                        most dominant color, and if that fails, it will revert to the secondary app
                        color.

                        When it has found the color or the object contained a color int. It will set
                        this color to the gradient that peeks out from under the poster.
                         */
                        if (mfg.getShadowInt() == -1) {
                            Bitmap b = ((BitmapDrawable) resource).getBitmap();

                            Palette.from(b).generate(palette -> {
                                int solid =
                                        palette.getVibrantColor(
                                                palette.getDarkVibrantColor(
                                                        palette.getDominantColor(mDefaultColor)));
                                shadow.setColorFilter(solid, PorterDuff.Mode.SRC_IN);
                                mMovieData.get(position).setShadowInt(solid);
                                mfg.setShadowInt(solid);
                            });
                        } else shadow.setColorFilter(mfg.getShadowInt(), PorterDuff.Mode.SRC_IN);

                        super.onResourceReady(resource, transition);
                    }

                    @Override
                    protected void setResource(@Nullable Drawable resource) {
                        //Intentionally empty as drawable is not ready
                    }
                });
    }

    @Override
    public int getItemCount() {
        if (mMovieData == null) return 0;
        return mMovieData.size();
    }


    public void setMovieData(List<Movie> movies) {
        if (movies == null) {
            return;
        }
        mMovieData = movies;
        notifyDataSetChanged();
    }

    public void setConnectionState(Boolean offlineMode) {
        mOffline = offlineMode;
    }

    public class PosterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_poster)
        ImageView mIv;
        @BindView(R.id.v_card_shadow)
        ImageView mShadowLayer;
        @BindView(R.id.cv_poster_wrapper)
        CardView mCvWrapper;
        @BindView(R.id.tv_movie_title_list)
        TextView mTvTitle;
        @BindView(R.id.pb_list_vote)
        ProgressBar mPbVoteBar;
        @BindView(R.id.tv_list_vote)
        TextView mTvVote;
        final GradientDrawable mGradientDrawable;


        PosterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mGradientDrawable = (GradientDrawable) ResourcesCompat.getDrawable(
                    itemView.getResources(),
                    R.drawable.fg_gradient,
                    itemView.getContext().getTheme());
        }

        /**
         * Sets the shared element transition name of the ImageView containing the poster and the
         * CardView wrapping the ImageView.
         *
         * It uses the poster url from the API to set an unique transition name.
         * It also uses the listType to set the name. Since the movie can appear in multiple lists.
         *
         * @param movie The movie belonging to the ViewHolder.
         */
        void setTransitionNames(Movie movie){
            mCvWrapper.setTransitionName(mListType + movie.getPosterUrl());
            mIv.setTransitionName("poster" + mListType + movie.getPosterUrl());
        }

    }


}
