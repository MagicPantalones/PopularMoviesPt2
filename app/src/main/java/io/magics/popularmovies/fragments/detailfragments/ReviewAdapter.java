package io.magics.popularmovies.fragments.detailfragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.ReviewResult;
import ru.noties.markwon.Markwon;


public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final List<ReviewResult> mReviewData = new ArrayList<>();


    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_detail_review_view_holder, parent, false);
        return new ReviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewResult review = mReviewData.get(position);

        holder.mTvAuthor.setText(review.getAuthor());

        Markwon.setMarkdown(holder.mMdvReview, review.getContent());

    }

    @Override
    public int getItemCount() {
        if (mReviewData.isEmpty()) return 0;
        return mReviewData.size();
    }

    public void setReviewData(List<ReviewResult> reviewData){
        if (reviewData == null || reviewData.isEmpty()) return;
        mReviewData.addAll(reviewData);
        notifyDataSetChanged();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.mdv_review_text) TextView mMdvReview;
        @BindView(R.id.tv_author) TextView mTvAuthor;


        ReviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
