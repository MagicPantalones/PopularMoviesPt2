package io.magics.popularmovies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

    public interface PosterClickHandler{
        void onClick(Movie movie, View view);
    }

    //Help from https://medium.com/@ayhamorfali/android-detect-when-the-recyclerview-reaches-the-bottom-43f810430e1e
    public interface ReachedEndHandler{
        void endReached(int position);
    }

    public PosterAdapter(PosterClickHandler posterClickHandler) {
        this.mClickHandler = posterClickHandler;
    }

    public void setEndListener(ReachedEndHandler reachedEndHandler){
        this.mReachedEndHandler = reachedEndHandler;
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        Boolean orientation = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        mImageSize = MovieUtils.getOptimalImgSize(context);

        //Sets the ViewHolder sizes based on the devise's orientation.
        mViewHeight = orientation ? parent.getMeasuredHeight() / 2 : parent.getMeasuredHeight();
        mViewWidth = orientation ? parent.getMeasuredWidth() / 2 : parent.getMeasuredWidth() / 3;

        View v = LayoutInflater.from(context).inflate(R.layout.poster_view_holder, parent, false);

        return new PosterViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        String posterUrl;
        Movie mfg = mMovieData.get(position);
        ImageView iv = holder.mIv;
        CardView cvWrapper = holder.mCvWrapper;
        TextView tvTitle = holder.mTvTitle;
        ProgressBar pbVotes = holder.mPbVoteBar;
        TextView tvVotes = holder.mTvVote;
        View shadow = holder.mShadowLayer;
        GradientDrawable gradientDrawable = (GradientDrawable) shadow.getBackground();

        posterUrl = ApiUtils.posterUrlConverter(mImageSize, mfg.getPosterUrl());
        if (position == mMovieData.size() - 5 && mReachedEndHandler != null){
            mReachedEndHandler.endReached(position);
        }

        cvWrapper.setMinimumWidth(mViewWidth);
        cvWrapper.setMinimumHeight(mViewHeight);

        tvTitle.setText(mfg.getTitle());
        pbVotes.setProgress(mfg.getVoteAverage().intValue() * 10);
        tvVotes.setText(Double.toString(mfg.getVoteAverage()));
        iv.setContentDescription(mfg.getTitle());

        if (mfg.getShadowInt().length == 0) {
            GlideApp.with(iv)
                    .asBitmap()
                    .load(posterUrl)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .override(mViewWidth, mViewHeight)
                    .centerCrop()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Palette palette = new Palette.Builder(resource).generate();
                            int defColor = Color.parseColor("#e96a6a");
                            int color = palette.getVibrantColor(
                                    palette.getDarkVibrantColor(
                                    palette.getDominantColor(defColor)));

                            Paint transparentColor = new Paint(0);
                            transparentColor.setColor(color);
                            transparentColor.setAlpha(0);

                            int[] colors = new int[]{color, transparentColor.getColor()};

                            gradientDrawable.setColors(colors);
                            shadow.setBackground(gradientDrawable);
                            mfg.setShadowInt(colors);

                            mMovieData.remove(position);
                            mMovieData.add(position, mfg);

                            iv.setImageBitmap(resource);
                        }
                    });
        } else {
            gradientDrawable.setColors(mfg.getShadowInt());
            shadow.setBackground(gradientDrawable);
            GlideApp.with(iv)
                    .load(posterUrl)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .override(mViewWidth, mViewHeight)
                    .centerCrop()
                    .into(iv);
        }
    }

    @Override
    public int getItemCount() {
        if (mMovieData == null) return 0;
        return mMovieData.size();
    }

    public void setMovieData(List<Movie> movies, Boolean listFromCursor){
        if (movies == null) {
            return;
        }else if (mMovieData == null || listFromCursor){
            mMovieData = null;
            mMovieData = movies;
        } else {
            mMovieData.addAll(movies);
        }
        notifyDataSetChanged();
    }

    public class PosterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iv_poster) ImageView mIv;
        @BindView(R.id.v_card_shadow) View mShadowLayer;
        @BindView(R.id.cv_view_holder_wrapper) CardView mCvWrapper;
        @BindView(R.id.tv_movie_title_list) TextView mTvTitle;
        @BindView(R.id.pb_vote_list) ProgressBar mPbVoteBar;
        @BindView(R.id.tv_vote_list) TextView mTvVote;

        public PosterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mClickHandler.onClick(mMovieData.get(getAdapterPosition()), v);
        }

    }


}
