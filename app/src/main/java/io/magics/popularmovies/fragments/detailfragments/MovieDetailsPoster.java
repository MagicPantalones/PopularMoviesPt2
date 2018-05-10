package io.magics.popularmovies.fragments.detailfragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

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
    private static final String ARG_TRANSITION_ID = "transitionId";

    private Movie mMovie;
    private String mTransitionId;

    @BindView(R.id.iv_poster_details)
    ImageView mPoster;
    @BindView(R.id.tv_details_title)
    TextView mTitle;
    @BindView(R.id.tv_details_release)
    TextView mReleaseDate;

    Unbinder mUnbinder;

    public MovieDetailsPoster() {
        // Required empty public constructor
    }


    public static MovieDetailsPoster newInstance(Movie movie, String transitionId) {
        MovieDetailsPoster fragment = new MovieDetailsPoster();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        args.putString(ARG_TRANSITION_ID, transitionId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
            mTransitionId = getArguments().getString(ARG_TRANSITION_ID);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_detail_poster, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        return root;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getContext();

        view.findViewById(R.id.nested_poster_wrapper).setTransitionName(mTransitionId);

        GlideApp.with(this)
                .load(posterUrlConverter(getOptimalImgSize(context), mMovie.getPosterUrl()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontTransform()
                .override(Target.SIZE_ORIGINAL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        getParentFragment().startPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        getParentFragment().startPostponedEnterTransition();
                        return false;
                    }
                })
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
