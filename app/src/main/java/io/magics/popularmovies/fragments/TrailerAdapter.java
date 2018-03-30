package io.magics.popularmovies.fragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.TrailerResult;
import io.magics.popularmovies.networkutils.ApiUtils;
import io.magics.popularmovies.utils.GlideApp;


public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private List<TrailerResult> mTrailerList = new ArrayList<>();
    private OnTrailerSelect mTrailerClickListener;

    public interface OnTrailerSelect{
        void onTrailerSelect(TrailerResult trailerResult);
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
        String trailerImgUrl = ApiUtils.youtubeStillUrlConverter(trailer.getKey());

        trailerIv.setContentDescription(trailer.getName());
        GlideApp.with(trailerIv)
                .load(trailerImgUrl)
                .centerCrop()
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

    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.iv_trailer_img)
        ImageView ivTrailerImg;

        public TrailerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mTrailerClickListener.onTrailerSelect(mTrailerList.get(getAdapterPosition()));
        }
    }
}
