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

    private String mListType;

    public interface ListItemEventHandler {
        void onClick(View holder, Movie movie, String transitionIdentifier, int selectedPosition);
        void onImageLoaded(int adapterPosition);
    }

    //Help from https://medium.com/@ayhamorfali/android-detect-when-the-recyclerview-reaches-the-bottom-43f810430e1e
    interface ReachedEndHandler {
        void endReached();
    }

    ListAdapter(ListItemEventHandler listItemEventHandler) {
        this.mClickHandler = listItemEventHandler;
        mListType = "favourites";
    }

    ListAdapter(ListItemEventHandler listItemEventHandler, ViewModel viewModel, int listType) {
        this.mClickHandler = listItemEventHandler;
        mReachedEndHandler = () -> {
            if (viewModel instanceof TopListViewModel) ((TopListViewModel) viewModel).notifyGetMoreTopPages();
            else if (viewModel instanceof PopListViewModel) ((PopListViewModel) viewModel).notifyGetMorePopPages();
        };
        if (listType == ListFragment.TOP_FRAGMENT) mListType = "topRated";
        else if (listType == ListFragment.POP_FRAGMENT) mListType = "popular";
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        mDefaultColor = ResourcesCompat.getColor(context.getResources(), R.color.colorSecondary, context.getTheme());
        mImageSize = MovieUtils.getOptimalImgSize(context);
        mTransitionStarted = new AtomicBoolean();

        View v = LayoutInflater.from(context).inflate(R.layout.fragment_list_view_holder, parent, false);

        v.measure(parent.getMeasuredWidth(), parent.getMeasuredHeight());

        int padding = v.findViewById(R.id.parent_wrapper_list).getPaddingStart() * 4;

        mPosterHeight = (v.getMeasuredWidth() - padding) / 2 * 3;
        mPosterWidth = mPosterHeight / 3 * 2;


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

        if (position == mMovieData.size() - 5 && mReachedEndHandler != null) {
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
                .placeholder(R.drawable.bg_loading_realydarkgrey)
                .dontTransform()
                .override(Target.SIZE_ORIGINAL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        if (MovieListsActivity.selectedPosition == position &&
                                !mTransitionStarted.getAndSet(true)) {
                            mClickHandler.onImageLoaded(position);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (MovieListsActivity.selectedPosition == position &&
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
        GradientDrawable mGradientDrawable;


        public PosterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mGradientDrawable = (GradientDrawable) ResourcesCompat.getDrawable(
                    itemView.getResources(),
                    R.drawable.fg_gradient,
                    itemView.getContext().getTheme());
        }

        void setTransitionNames(Movie movie){
            mCvWrapper.setTransitionName(mListType + movie.getPosterUrl());
            mIv.setTransitionName("poster" + mListType + movie.getPosterUrl());
        }

    }


}
