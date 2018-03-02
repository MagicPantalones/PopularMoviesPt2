package io.magics.popularmovies;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.networkutils.ApiUtils;
import io.magics.popularmovies.utils.GlideApp;
import io.magics.popularmovies.utils.MovieUtils;
import io.magics.popularmovies.utils.ThreadingUtils;
import io.reactivex.disposables.Disposable;


public class MovieDetailsActivity extends AppCompatActivity {

    Movie mMovie;
    int mImageWidth;
    int mImageHeight;
    MovieUtils.ImageSize mImageSize;
    Disposable mDisposable;
    Disposable mDisp;
    List<Integer> mFavIds;

    @BindView(R.id.iv_poster_details) ImageView mPosterIv;
    @BindView(R.id.tv_movie_title) TextView mTitleTv;
    @BindView(R.id.tv_release_date_text) TextView mReleaseDateTv;
    @BindView(R.id.tv_vote_average_text) TextView mVoteTv;
    @BindView(R.id.tv_plot_text) TextView mPlotTv;
    @BindView(R.id.fab) FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        mImageSize = MovieUtils.getOptimalImgSize(this);

        Intent intent = getIntent();
        Bundle bundle;
        if (intent != null){
            bundle = intent.getExtras();
            mImageHeight = bundle.getInt("height");
            mImageWidth = bundle.getInt("width");
            mMovie = bundle.getParcelable("movie");
        }

        mTitleTv.setText(mMovie.getTitle());
        mReleaseDateTv.setText(MovieUtils.formatDate(mMovie.getReleaseDate()));
        mVoteTv.setText(getString(
                R.string.details_vote_average_text,
                Double.toString(mMovie.getVoteAverage()),
                mMovie.getVoteCount()));
        mPlotTv.setText(mMovie.getOverview());
        String posterUrl = ApiUtils.posterUrlConverter(mImageSize, mMovie.getPosterUrl());
        GlideApp.with(mPosterIv)
                .load(posterUrl)
                .centerCrop()
                .override(720, mImageHeight)
                .placeholder(R.drawable.bg_loading_realydarkgrey)
                .into(mPosterIv);

        ThreadingUtils.queryForFavourites(this, movieIdList -> {
            mFavIds = movieIdList;
            mFab.setImageResource(ThreadingUtils.checkIfFav(mMovie.getMovieId(), mFavIds) ?
                    R.drawable.ic_bookmark_black_24dp : R.drawable.ic_bookmark_border_black_24dp);
        });

    }

    @OnClick(R.id.fab)
    public void onFabPressed(FloatingActionButton button){
        ThreadingUtils.addToFavourites(this, mMovie, mFavIds, id -> {
            button.setImageResource(id > 0 ? R.drawable.ic_bookmark_black_24dp : R.drawable.ic_bookmark_border_black_24dp);
        });
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null && !mDisposable.isDisposed()) mDisposable.dispose();
        if (mDisp != null && !mDisp.isDisposed()) mDisp.dispose();
        super.onDestroy();
    }
}
