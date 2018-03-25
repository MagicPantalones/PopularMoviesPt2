package io.magics.popularmovies.fragments;


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

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.TrailerResult;
import io.magics.popularmovies.models.Trailers;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MovieDetailsTrailers#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieDetailsTrailers extends Fragment
    implements TrailerAdapter.OnTrailerSelect{

    private static final String ARG_TRAILER_LIST = "trailers";

    private List<TrailerResult> mTrailers = new ArrayList<>();


    public MovieDetailsTrailers() {
        // Required empty public constructor
    }

    public static MovieDetailsTrailers newInstance(Trailers trailers) {
        MovieDetailsTrailers fragment = new MovieDetailsTrailers();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRAILER_LIST, trailers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Trailers trailers = getArguments().getParcelable(ARG_TRAILER_LIST);
            mTrailers.addAll(trailers.getTrailerResults());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail_trailers, container, false);
        ButterKnife.bind(this, root);

        RecyclerView trailerRecycler = root.findViewById(R.id.rv_trailers);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        TrailerAdapter adapter = new TrailerAdapter(this, container.getWidth(), container.getHeight());

        trailerRecycler.setAdapter(adapter);
        trailerRecycler.setLayoutManager(manager);

        adapter.setTrailerList(mTrailers);

        return root;
    }

    @Override
    public void onTrailerSelect(TrailerResult trailerResult) {
        Intent ytAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailerResult.getKey()));

        try {
            getContext().startActivity(ytAppIntent);
        } catch (ActivityNotFoundException e){
            Intent ytWebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailerResult.getKey()));
            getContext().startActivity(ytWebIntent);
        }

    }
}
