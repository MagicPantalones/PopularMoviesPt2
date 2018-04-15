package io.magics.popularmovies.fragments.detailfragments;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.TrailerResult;
import io.magics.popularmovies.models.Trailers;
import io.magics.popularmovies.utils.MovieUtils;


public class MovieDetailsTrailers extends Fragment
    implements TrailerAdapter.OnTrailerSelect{

    private List<TrailerResult> mTrailers = new ArrayList<>();

    @BindView(R.id.rv_trailers) RecyclerView mTrailerRecycler;
    @BindView(R.id.tv_no_trailers) TextView mTvNoTrailers;

    private Unbinder mUnbinder;

    public MovieDetailsTrailers() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail_trailers, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        if (mTrailers.isEmpty()){
            MovieUtils.hideAndShowView(mTvNoTrailers, mTrailerRecycler);
        } else {
            LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            TrailerAdapter adapter = new TrailerAdapter(this);
            mTrailerRecycler.setAdapter(adapter);
            mTrailerRecycler.setLayoutManager(manager);
            adapter.setTrailerList(mTrailers);
        }
        return root;
    }

    @Override
    public void onDestroy() {
        if (mUnbinder != null) mUnbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onTrailerSelect(TrailerResult trailerResult) {
        Intent ytAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailerResult.getKey()));
        try {
            Objects.requireNonNull(getContext()).startActivity(ytAppIntent);
        } catch (ActivityNotFoundException e){
            Intent ytWebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailerResult.getKey()));
            Objects.requireNonNull(getContext()).startActivity(ytWebIntent);
        }

    }
}
