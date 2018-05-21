package io.magics.popularmovies.fragments.detailfragments;


import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
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
import io.magics.popularmovies.models.TrailerResult;
import io.magics.popularmovies.viewmodels.TrailersViewModel;

import static io.magics.popularmovies.utils.MovieUtils.toggleViewVisibility;


public class MovieDetailsTrailers extends Fragment
        implements TrailerAdapter.OnTrailerSelect {

    @BindView(R.id.rv_trailers)
    RecyclerView mRvTrailerRecycler;
    @BindView(R.id.tv_no_trailers)
    TextView mTvNoTrailers;

    private TrailersViewModel mViewModel;

    private Unbinder mUnbinder;

    public MovieDetailsTrailers() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail_trailers, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        //noinspection ConstantConditions
        mViewModel = ViewModelProviders.of(getActivity()).get(TrailersViewModel.class);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TrailerAdapter adapter = new TrailerAdapter(this);

        mRvTrailerRecycler.setAdapter(adapter);

        //noinspection ConstantConditions
        if (getContext().getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayoutManager manager =
                    (LinearLayoutManager) mRvTrailerRecycler.getLayoutManager();
            manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        }

        //noinspection ConstantConditions
        mViewModel.mTrailers.observe(getActivity(), trailerResults -> {
            if (trailerResults != null && !trailerResults.isEmpty()) {

                toggleViewVisibility(mRvTrailerRecycler, mTvNoTrailers);
                adapter.setTrailerList(trailerResults);

            }
        });

    }

    @Override
    public void onDestroy() {
        if (mUnbinder != null) mUnbinder.unbind();
        super.onDestroy();
    }

    /*
     * Get's the intent from the Adapter, and starts a YouTube activity. If the user does not have
     * YouTube installed it will launch the user's web-browser with the YouTube link
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onPlayTrailer(TrailerResult trailerResult) {
        Intent ytAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:"
                + trailerResult.getKey()));
        try {
            getContext().startActivity(ytAppIntent);
        } catch (ActivityNotFoundException e) {
            Intent ytWebIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + trailerResult.getKey()));
            getContext().startActivity(ytWebIntent);
        }

    }

    //Shares the YouTube URL for the trailer.
    @Override
    public void onShareTrailer(Intent trailer) {
        //noinspection ConstantConditions
        getContext().startActivity(trailer);
    }
}
