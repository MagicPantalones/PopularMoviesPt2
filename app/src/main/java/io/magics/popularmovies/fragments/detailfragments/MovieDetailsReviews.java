package io.magics.popularmovies.fragments.detailfragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.R;
import io.magics.popularmovies.viewmodels.ReviewsViewModel;

import static io.magics.popularmovies.utils.MovieUtils.toggleViewVisibility;


public class MovieDetailsReviews extends Fragment {

    @BindView(R.id.rv_reviews) RecyclerView mRvReviewRecycler;
    @BindView(R.id.tv_no_reviews) TextView mTvNoReviews;

    private ReviewsViewModel mViewModel;

    private Unbinder mUnbinder;

    public MovieDetailsReviews() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail_reviews, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        //noinspection ConstantConditions
        mViewModel = ViewModelProviders.of(getActivity()).get(ReviewsViewModel.class);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ReviewAdapter adapter = new ReviewAdapter();

        mRvReviewRecycler.setLayoutManager(new LinearLayoutManager(view.getContext(),
                LinearLayoutManager.VERTICAL, false));
        mRvReviewRecycler.setAdapter(adapter);
        mRvReviewRecycler.setNestedScrollingEnabled(false);

        //noinspection ConstantConditions
        mViewModel.mReviews.observe(getActivity(), reviewResults -> {
            if (reviewResults != null){
                if (reviewResults.isEmpty()) toggleViewVisibility(mTvNoReviews, mRvReviewRecycler);
                else {
                    toggleViewVisibility(View.VISIBLE, mRvReviewRecycler, mTvNoReviews);
                    adapter.setReviewData(reviewResults);
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        mUnbinder.unbind();
        super.onDestroy();
    }


}
