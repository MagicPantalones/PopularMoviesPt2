package io.magics.popularmovies.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import us.feras.mdv.MarkdownView;


public class MovieDetailsReviews extends Fragment {

    private static final String ARG_REVIEWS = "reviews";

    private List<ReviewResult> mReviews = new ArrayList<>();

    @BindView(R.id.mdv_review_text) MarkdownView mMdvReview;
    @BindView(R.id.tv_author) TextView mTvAuthor;

    private Unbinder mUnbinder;

    public MovieDetailsReviews() {
        // Required empty public constructor
    }

    public static MovieDetailsReviews newInstance(Reviews reviews) {
        MovieDetailsReviews fragment = new MovieDetailsReviews();
        Bundle args = new Bundle();
        args.putParcelable(ARG_REVIEWS, reviews);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Reviews reviews = getArguments().getParcelable(ARG_REVIEWS);
            mReviews.addAll(reviews.getReviewResults());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail_reviews, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        return root;
    }

    @Override
    public void onDestroy() {
        mUnbinder.unbind();
        super.onDestroy();
    }
}
