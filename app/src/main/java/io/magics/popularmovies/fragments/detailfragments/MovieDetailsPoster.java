package io.magics.popularmovies.fragments.detailfragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.GlideApp;

import static io.magics.popularmovies.utils.MovieUtils.formatDate;
import static io.magics.popularmovies.utils.MovieUtils.getOptimalImgSize;
import static io.magics.popularmovies.utils.MovieUtils.posterUrlConverter;

public class MovieDetailsPoster extends Fragment {

    private static final String ARG_MOVIE = "passedMovie";

    private Movie mMovie;

    @BindView(R.id.detail_fragment_poster)
    ImageView mPoster;
    @BindView(R.id.wrapper_card_details_poster)
    CardView mPosterWrapperCard;
    @BindView(R.id.tv_details_title)
    TextView mTitle;
    @BindView(R.id.tv_details_release)
    TextView mReleaseDate;

    Unbinder mUnbinder;

    public MovieDetailsPoster() {
        // Required empty public constructor
    }


    public static MovieDetailsPoster newInstance(Movie movie) {
        MovieDetailsPoster fragment = new MovieDetailsPoster();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_detail_poster, container, false);
        mUnbinder = ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getContext();

        GlideApp.with(this)
                .load(posterUrlConverter(getOptimalImgSize(context), mMovie.getPosterUrl()))
                .centerCrop()
                .placeholder(R.drawable.bg_loading_realydarkgrey)
                .dontAnimate()
                .into(mPoster);

        mTitle.setText(mMovie.getTitle());
        mReleaseDate.setText(formatDate(mMovie.getReleaseDate()));

    }

    @Override
    public void onDetach() {
        if (mUnbinder != null) mUnbinder.unbind();
        super.onDetach();
    }
}
