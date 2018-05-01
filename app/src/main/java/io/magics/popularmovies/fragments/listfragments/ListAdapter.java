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
import android.widget.FrameLayout;
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

    private ListFragment mFragment;

    public interface PosterClickHandler {
        void onClick(View v, Movie movie, int position);
    }

    //Help from https://medium.com/@ayhamorfali/android-detect-when-the-recyclerview-reaches-the-bottom-43f810430e1e
    interface ReachedEndHandler {
        void endReached();
    }

    public ListAdapter(PosterClickHandler posterClickHandler, ListFragment fragment) {
        this.mClickHandler = posterClickHandler;
        mFragment = fragment;
    }

    public ListAdapter(PosterClickHandler posterClickHandler, ViewModel viewModel, ListFragment fragment) {
        this.mClickHandler = posterClickHandler;
        mFragment = fragment;
        mReachedEndHandler = () -> {
            if (viewModel instanceof TopListViewModel) ((TopListViewModel) viewModel).notifyGetMoreTopPages();
            else if (viewModel instanceof PopListViewModel) ((PopListViewModel) viewModel).notifyGetMorePopPages();
        };
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

        cvWrapper.setTransitionName(mfg.getMovieId().toString());

        posterUrl = MovieUtils.posterUrlConverter(mImageSize, mfg.getPosterUrl());
        if (position == mMovieData.size() - 5 && mReachedEndHandler != null) {
            mReachedEndHandler.endReached();
        }

        //How to get compatPadding dimens taken from Janholds answer here:
        //https://stackoverflow.com/questions/34656252/cardview-cardusecompatpadding

        double cos45 = Math.cos(Math.toRadians(45));
        float elevation = cvWrapper.getCardElevation();
        float radius = cvWrapper.getRadius();
        int compatPad = (int) ((elevation + (1 - cos45) * radius) +
                (elevation * 1.5 + (1 - cos45) * radius));
        int centerImg = ((ViewGroup) cvWrapper.getParent()).getPaddingStart() * 2 + compatPad * 2;

        iv.setLayoutParams(new FrameLayout.LayoutParams(mViewWidth - centerImg, mViewHeight - centerImg));

        tvTitle.setText(mfg.getTitle());
        pbVotes.setProgress(mfg.getVoteAverage().intValue() * 10);
        tvVotes.setText(Double.toString(mfg.getVoteAverage()));
        iv.setContentDescription(mfg.getTitle());
        shadow.setImageDrawable(holder.mGradientDrawable.mutate());

        GlideApp.with(iv)
                .load(posterUrl)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(R.drawable.bg_loading_realydarkgrey)
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
                                mMovieData.get(holder.getAdapterPosition()).setShadowInt(solid);
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

    public List<Movie> getMovieData(){
        return mMovieData;
    }

    public class PosterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
            mClickHandler.onClick(v, mMovieData.get(getAdapterPosition()), getAdapterPosition());

        }

    }


}
