package io.magics.popularmovies;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    @BindView(R.id.tv_release_date_detail) TextView mReleaseDateTv;
    @BindView(R.id.tv_vote_average_detail) TextView mVoteTv;
    @BindView(R.id.fav_fab) FloatingActionButton mFab;
    @BindView(R.id.pb_vote_count_detail)
    ProgressBar mVoteProgress;

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
            if (bundle != null) {
                mMovie = bundle.getParcelable("movie");
            }
        }

        mTitleTv.setText(mMovie.getTitle());
        mReleaseDateTv.setText(MovieUtils.formatDate(mMovie.getReleaseDate()));

        Long voteCalcLong = Math.round(mMovie.getVoteAverage() * 10);
        ValueAnimator voteTextAnim = ValueAnimator.ofFloat(0.0f, mMovie.getVoteAverage().floatValue());
        ObjectAnimator voteProgressAnim = ObjectAnimator.ofInt(mVoteProgress, "progress", voteCalcLong.intValue());

        voteTextAnim.setDuration(2000);
        voteTextAnim.setInterpolator(new BounceInterpolator());
        voteTextAnim.addUpdateListener(valueAnimator -> {
            String shownVal = valueAnimator.getAnimatedValue().toString();
            shownVal = shownVal.substring(0, 3);
            mVoteTv.setText(shownVal);
        });
        voteTextAnim.start();


        voteProgressAnim.setDuration(2000);
        voteProgressAnim.setInterpolator(new BounceInterpolator());
        voteProgressAnim.start();

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
                    R.drawable.ic_favourite_heart_red : R.drawable.ic_favourite_heart_gray);
        });

    }

    @OnClick(R.id.fav_fab)
    public void onFabPressed(FloatingActionButton button){
        ThreadingUtils.addToFavourites(this, mMovie, mFavIds, id -> {
            button.setImageResource(id > 0 ? R.drawable.ic_favourite_heart_red : R.drawable.ic_favourite_heart_gray);
        });
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null && !mDisposable.isDisposed()) mDisposable.dispose();
        if (mDisp != null && !mDisp.isDisposed()) mDisp.dispose();
        super.onDestroy();
    }
}
