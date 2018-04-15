package io.magics.popularmovies.fragments.listfragments;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.res.Configuration;
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

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.GlideApp;
import io.magics.popularmovies.utils.MovieUtils;
import io.magics.popularmovies.viewmodels.PopListViewModel;
import io.magics.popularmovies.viewmodels.TopListViewModel;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.PosterViewHolder> {

    private List<Movie> mMovieData = new ArrayList<>();
    private int mViewWidth;
    private int mViewHeight;
    private final PosterClickHandler mClickHandler;
    private ReachedEndHandler mReachedEndHandler;
    private MovieUtils.ImageSize mImageSize;
    private int mDefaultColor;

    public interface PosterClickHandler {
        void onClick(Movie movie);
    }

    //Help from https://medium.com/@ayhamorfali/android-detect-when-the-recyclerview-reaches-the-bottom-43f810430e1e
    public interface ReachedEndHandler {
        void endReached();
    }

    public ListAdapter(PosterClickHandler posterClickHandler) {
        this.mClickHandler = posterClickHandler;
    }

    public ListAdapter(PosterClickHandler posterClickHandler, ViewModel viewModel) {
        this.mClickHandler = posterClickHandler;
        mReachedEndHandler = () -> {
            if (viewModel instanceof TopListViewModel) ((TopListViewModel) viewModel).notifyGetMoreTopPages();
            else if (viewModel instanceof PopListViewModel) ((PopListViewModel) viewModel).notifyGetMorePopPages();
        };
    }

    public void setEndListener(ReachedEndHandler reachedEndHandler) {
        this.mReachedEndHandler = reachedEndHandler;
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        Boolean orientation = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        mDefaultColor = ResourcesCompat.getColor(context.getResources(), R.color.colorSecondary, context.getTheme());
        mImageSize = MovieUtils.getOptimalImgSize(context);


        //Sets the ViewHolder sizes based on the devise's orientation.
        mViewHeight = orientation ? parent.getMeasuredHeight() / 2 : parent.getMeasuredHeight();
        mViewWidth = orientation ? parent.getMeasuredWidth() / 2 : parent.getMeasuredWidth() / 3;

        View v = LayoutInflater.from(context).inflate(R.layout.fragment_list_view_holder, parent, false);

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
        ImageView shadow = holder.mShadowLayer;

        posterUrl = MovieUtils.posterUrlConverter(mImageSize, mfg.getPosterUrl());
        if (position == mMovieData.size() - 5 && mReachedEndHandler != null) {
            mReachedEndHandler.endReached();
        }

        cvWrapper.setMinimumWidth(mViewWidth);
        cvWrapper.setMinimumHeight(mViewHeight);

        tvTitle.setText(mfg.getTitle());
        pbVotes.setProgress(mfg.getVoteAverage().intValue() * 10);
        tvVotes.setText(Double.toString(mfg.getVoteAverage()));
        iv.setContentDescription(mfg.getTitle());
        shadow.setImageDrawable(holder.mGradientDrawable.mutate());

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


    public void setMovieData(List<Movie> movies, int position) {
        if (movies == null) {
            return;
        }
        if (position == 0){
            mMovieData.clear();
            mMovieData.addAll(movies);
            notifyDataSetChanged();
        } else {
            mMovieData.addAll(movies);
            notifyItemInserted(position);
        }
    }

    public class PosterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iv_poster)
        ImageView mIv;
        @BindView(R.id.v_card_shadow)
        ImageView mShadowLayer;
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
            mClickHandler.onClick(mMovieData.get(getAdapterPosition()));
        }

    }


}
