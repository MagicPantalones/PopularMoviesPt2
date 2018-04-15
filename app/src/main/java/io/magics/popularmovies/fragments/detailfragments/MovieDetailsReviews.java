package io.magics.popularmovies.fragments.detailfragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.ReviewResult;
import io.magics.popularmovies.models.Reviews;
import io.magics.popularmovies.utils.MovieUtils;


public class MovieDetailsReviews extends Fragment {

    private List<ReviewResult> mReviews = new ArrayList<>();

    @BindView(R.id.rv_reviews) RecyclerView mRvReviewRecycler;
    @BindView(R.id.tv_no_reviews) TextView mTvNoReviews;

    private Unbinder mUnbinder;

    public MovieDetailsReviews() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail_reviews, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        if (mReviews.isEmpty()){
            MovieUtils.hideAndShowView(mTvNoReviews, mRvReviewRecycler);
        } else {
            LinearLayoutManager manager = new LinearLayoutManager(root.getContext(), LinearLayoutManager.VERTICAL, false);
            ReviewAdapter adapter = new ReviewAdapter();

            mRvReviewRecycler.setLayoutManager(manager);
            mRvReviewRecycler.setAdapter(adapter);
            adapter.setReviewData(mReviews);
        }
        return root;
    }

    @Override
    public void onDestroy() {
        mUnbinder.unbind();
        super.onDestroy();
    }
}
