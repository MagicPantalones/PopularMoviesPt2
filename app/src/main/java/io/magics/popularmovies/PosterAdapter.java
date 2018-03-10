package io.magics.popularmovies;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;

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

    private static final String TAG = PosterAdapter.class.getSimpleName();
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

    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        String posterUrl;
        Movie mfg = mMovieData.get(position);
        ImageView iv = holder.mIv;

        posterUrl = ApiUtils.posterUrlConverter(mImageSize, mfg.getPosterUrl());
        if (position == mMovieData.size() - 1 && mReachedEndHandler != null){
            mReachedEndHandler.endReached(position);
        }

        iv.setContentDescription(mfg.getTitle());

        GlideApp.with(holder.itemView)
                    .load(posterUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_loading_realydarkgrey)
                    .downsample(DownsampleStrategy.NONE)
                    .centerCrop()
                    .override(mViewWidth, mViewHeight)
                    .into(iv);
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
