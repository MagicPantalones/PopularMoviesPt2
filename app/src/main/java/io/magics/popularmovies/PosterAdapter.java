package io.magics.popularmovies;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.networkutils.ApiUtils;
import io.magics.popularmovies.utils.GlideApp;
import io.magics.popularmovies.utils.MovieUtils;

/**
 * Adapter for my recycler
 * Created by Erik on 18.02.2018.
 */

public class PosterAdapter extends RecyclerView.Adapter<PosterAdapter.PosterViewHolder> {

    private List<Movie> mMovieData;
    private int mViewWidth;
    private int mViewHeight;
    private final PosterClickHandler mClickHandler;
    private ReachedEndHandler mReachedEndHandler;
    private MovieUtils.ImageSize mImageSize;
    private GradientDrawable mPosterShadow;
    private Context mContext;
    private int[] mColorReset = new int[]{Color.TRANSPARENT, Color.TRANSPARENT};

    public interface PosterClickHandler {
        void onClick(Movie movie, int position);
    }

    //Help from https://medium.com/@ayhamorfali/android-detect-when-the-recyclerview-reaches-the-bottom-43f810430e1e
    public interface ReachedEndHandler {
        void endReached(int position);
    }

    public PosterAdapter(PosterClickHandler posterClickHandler) {
        this.mClickHandler = posterClickHandler;
    }

    public void setEndListener(ReachedEndHandler reachedEndHandler) {
        this.mReachedEndHandler = reachedEndHandler;
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        Boolean orientation = mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        mImageSize = MovieUtils.getOptimalImgSize(mContext);
        mPosterShadow = (GradientDrawable) ResourcesCompat.getDrawable(
                mContext.getResources(),
                R.drawable.fg_gradient,
                mContext.getTheme()
        );

        //Sets the ViewHolder sizes based on the devise's orientation.
        mViewHeight = orientation ? parent.getMeasuredHeight() / 2 : parent.getMeasuredHeight();
        mViewWidth = orientation ? parent.getMeasuredWidth() / 2 : parent.getMeasuredWidth() / 3;

        View v = LayoutInflater.from(mContext).inflate(R.layout.poster_view_holder, parent, false);

        return new PosterViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        final String posterUrl;
        final Movie mfg = mMovieData.get(position);
        final ImageView iv = holder.mIv;
        final CardView cvWrapper = holder.mCvWrapper;
        final TextView tvTitle = holder.mTvTitle;
        final ProgressBar pbVotes = holder.mPbVoteBar;
        final TextView tvVotes = holder.mTvVote;
        final View shadow = holder.mShadowLayer;

        posterUrl = ApiUtils.posterUrlConverter(mImageSize, mfg.getPosterUrl());
        if (position == mMovieData.size() - 5 && mReachedEndHandler != null) {
            mReachedEndHandler.endReached(position);
        }

        cvWrapper.setMinimumWidth(mViewWidth);
        cvWrapper.setMinimumHeight(mViewHeight);

        tvTitle.setText(mfg.getTitle());
        pbVotes.setProgress(mfg.getVoteAverage().intValue() * 10);
        tvVotes.setText(Double.toString(mfg.getVoteAverage()));
        iv.setContentDescription(mfg.getTitle());

        GlideApp.with(iv)
                .load(posterUrl)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(mViewWidth, mViewHeight)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(new ImageViewTarget<Drawable>(iv) {


                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        iv.setImageDrawable(resource.getCurrent());
                        if (mfg.getShadowInt().length == 0) {
                            Bitmap b = ((BitmapDrawable) iv.getDrawable().getCurrent()).getBitmap();
                            setColorBleed(b, holder, position);
                        } else {
                            mPosterShadow.setColors(mfg.getShadowInt());
                            shadow.setBackground(mPosterShadow);
                        }
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

    private void setColorBleed(final Bitmap bitmap, final PosterViewHolder holder, final int position) {

        Palette palette = Palette.from(bitmap).generate();
        int solid =
                palette.getVibrantColor(palette.getDarkVibrantColor(palette.getDominantColor(ResourcesCompat.getColor(
                        mContext.getResources(),
                        R.color.colorSecondary,
                        mContext.getTheme()))));

        int transparent = ColorUtils.setAlphaComponent(solid, 0);

        int[] colors = new int[]{solid, transparent};
        mPosterShadow.setColors(colors);
        holder.mShadowLayer.setBackground(mPosterShadow);
        mMovieData.get(position).setShadowInt(colors);

    }

    public void setMovieData(List<Movie> movies, int position, Boolean listFromCursor) {
        if (movies == null) {
            return;
        } else if (mMovieData == null || listFromCursor) {
            mMovieData = null;
            mMovieData = movies;
        } else {
            mMovieData.addAll(movies);
        }
        if (position == 0) notifyDataSetChanged();
        else notifyItemInserted(position);
    }

    public class PosterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iv_poster)
        ImageView mIv;
        @BindView(R.id.v_card_shadow)
        View mShadowLayer;
        @BindView(R.id.cv_view_holder_wrapper)
        CardView mCvWrapper;
        @BindView(R.id.tv_movie_title_list)
        TextView mTvTitle;
        @BindView(R.id.pb_vote_list)
        ProgressBar mPbVoteBar;
        @BindView(R.id.tv_vote_list)
        TextView mTvVote;
        GradientDrawable mGradientDrawable;


        public PosterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);

            mGradientDrawable = (GradientDrawable) ResourcesCompat.getDrawable(
                    itemView.getResources(),
                    R.drawable.fg_gradient,
                    itemView.getContext().getTheme());
        }

        @Override
        public void onClick(View v) {
            mClickHandler.onClick(mMovieData.get(getAdapterPosition()), getAdapterPosition());
        }

    }


}
