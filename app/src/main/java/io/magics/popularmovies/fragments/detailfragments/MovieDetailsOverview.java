package io.magics.popularmovies.fragments.detailfragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsOverview extends Fragment {

    private static final String ARG_MOVIE = "movie";

    private Movie mMovie;

    public static MovieDetailsOverview newInstance(Movie movie){
        MovieDetailsOverview frag = new MovieDetailsOverview();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        frag.setArguments(args);
        return frag;
    }

    public MovieDetailsOverview() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            mMovie = getArguments().getParcelable(ARG_MOVIE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail_overview, container, false);
        TextView tvOverview = root.findViewById(R.id.tv_overview_frag);
        tvOverview.setText(mMovie.getOverview());

        return root;
    }
}
