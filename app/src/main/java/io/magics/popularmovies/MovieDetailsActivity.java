package io.magics.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.networkutils.TMDBApiNetworkService;
import io.magics.popularmovies.utils.GlideApp;
import io.magics.popularmovies.utils.MovieUtils;

public class MovieDetailsActivity extends AppCompatActivity {

    Movie mMovie;
    int mImageWidth;
    int mImageHeight;
    TMDBApiNetworkService.ImageSize mImageSize;

    @BindView(R.id.iv_poster_details) ImageView mPosterIv;
    @BindView(R.id.tv_movie_title) TextView mTitleTv;
    @BindView(R.id.tv_release_date_text) TextView mReleaseDateTv;
    @BindView(R.id.tv_vote_average_text) TextView mVoteTv;
    @BindView(R.id.tv_plot_text) TextView mPlotTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        mImageSize = TMDBApiNetworkService.getOptimalImgSize(this);

        Intent intent = getIntent();
        Bundle bundle;
        if (intent != null){
            bundle = intent.getExtras();
            mImageHeight = bundle.getInt("height");
            mImageWidth = bundle.getInt("width");
            mMovie = bundle.getParcelable("movie");
        }

        mPosterIv.setMinimumWidth(mImageWidth);
        mPosterIv.setMinimumHeight(mImageHeight);

        mTitleTv.setText(mMovie.getTitle());
        mReleaseDateTv.setText(MovieUtils.formatDate(mMovie.getReleaseDate()));
        mVoteTv.setText(getString(
                R.string.details_vote_average_text,
                Double.toString(mMovie.getVoteAverage()),
                mMovie.getVoteCount()));
        mPlotTv.setText(mMovie.getOverview());
        String posterUrl = TMDBApiNetworkService.posterUrlConverter(mImageSize, mMovie.getPosterUrl());
        GlideApp.with(mPosterIv)
                .load(posterUrl)
                .centerCrop()
                .placeholder(R.drawable.bg_loading_realydarkgrey)
                .into(mPosterIv);
    }

}
