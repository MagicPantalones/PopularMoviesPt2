package io.magics.popularmovies.fragments.detailfragments;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.TrailerResult;
import io.magics.popularmovies.utils.GlideApp;
import io.magics.popularmovies.utils.MovieUtils;


public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private List<TrailerResult> mTrailerList = new ArrayList<>();
    private OnTrailerSelect mTrailerClickListener;

    public interface OnTrailerSelect{
        void onPlayTrailer(TrailerResult trailerResult);
        void onShareTrailer(Intent trailer);
    }

    public TrailerAdapter(OnTrailerSelect listener){
        this.mTrailerClickListener = listener;
    }

    @NonNull
    @Override
    public TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_detail_trailer_view_holder, parent, false);
        return new TrailerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerViewHolder holder, int position) {
        ImageView trailerIv = holder.ivTrailerImg;
        TrailerResult trailer = mTrailerList.get(position);
        String trailerImgUrl = MovieUtils.youtubeStillUrlConverter(trailer.getKey());

        trailerIv.setContentDescription(trailer.getName());

        holder.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "https://www.youtube.com/watch?v=" + trailer.getKey());
            mTrailerClickListener.onShareTrailer(shareIntent);
        });

        holder.btnPlay.setOnClickListener(v -> {
            mTrailerClickListener.onPlayTrailer(trailer);
        });

        GlideApp.with(trailerIv)
                .load(trailerImgUrl)
                .dontTransform()
                .into(trailerIv);

    }

    @Override
    public int getItemCount() {
        if (mTrailerList.isEmpty()) return 0;
        return mTrailerList.size();
    }

    public void setTrailerList(List<TrailerResult> trailerList){
        if (trailerList.isEmpty()) return;

        for (TrailerResult t : trailerList){
            if (t.getSite().equals("YouTube")) mTrailerList.add(t);
        }
        notifyDataSetChanged();
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_trailer_img)
        ImageView ivTrailerImg;
        @BindView(R.id.btn_share)
        Button btnShare;
        @BindView(R.id.btn_play)
        Button btnPlay;

        public TrailerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
