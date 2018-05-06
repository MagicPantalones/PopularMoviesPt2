package io.magics.popularmovies;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.transition.TransitionValues;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.fragments.detailfragments.MovieDetailsFragment;
import io.magics.popularmovies.fragments.detailfragments.MovieDetailsPoster;
import io.magics.popularmovies.fragments.listfragments.ListAdapter;
import io.magics.popularmovies.fragments.listfragments.ListFragment;
import io.magics.popularmovies.fragments.listfragments.ListTabLayout;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.networkutils.DataProvider;
import io.magics.popularmovies.utils.MovieUtils.ScrollDirection;
import io.magics.popularmovies.viewmodels.FavListViewModel;
import io.magics.popularmovies.viewmodels.PopListViewModel;
import io.magics.popularmovies.viewmodels.ReviewsViewModel;
import io.magics.popularmovies.viewmodels.TopListViewModel;
import io.magics.popularmovies.viewmodels.TrailersViewModel;

public class MovieListsActivity extends AppCompatActivity implements ListFragment.FragmentListener,
        MovieDetailsFragment.DetailFragInteractionHandler, ListTabLayout.TabLayoutPageEvents,
        ListAdapter.PosterClickHandler{

    //TODO Add horizontal layout support.
    //TODO Inspect for memoryleaks

    private static final String FRAG_PAGER_TAG = "pagerTag";
    private static final String DETAIL_FRAGMENT_TAG = "detailFrag";

    @BindView(R.id.up_fab)
    FloatingActionButton mUpFab;
    @BindView(R.id.container_main)
    ViewGroup mMainContainer;

    private DataProvider mDataProvider;

    private TopListViewModel mTopListVM;
    private PopListViewModel mPopListVM;
    private FavListViewModel mFavListVM;
    private TrailersViewModel mTrailerVm;
    private ReviewsViewModel mReviewVm;

    private Unbinder mUnbinder;

    private FragmentManager mAppFragManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_lists);
        mUnbinder = ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);

        mTopListVM = ViewModelProviders.of(this).get(TopListViewModel.class);
        mPopListVM = ViewModelProviders.of(this).get(PopListViewModel.class);
        mFavListVM = ViewModelProviders.of(this).get(FavListViewModel.class);
        mTrailerVm = ViewModelProviders.of(this).get(TrailersViewModel.class);
        mReviewVm = ViewModelProviders.of(this).get(ReviewsViewModel.class);

        mDataProvider = new DataProvider(this, mTopListVM, mPopListVM, mFavListVM,
                mTrailerVm, mReviewVm);

        mAppFragManager = getSupportFragmentManager();

        mDataProvider.initialiseApp();

        mUpFab.setOnClickListener(v -> {
            ListTabLayout tabFrag = (ListTabLayout) mAppFragManager
                    .findFragmentByTag(FRAG_PAGER_TAG);
            if (tabFrag != null) tabFrag.notifyUpFabPressed();
        });

        if (savedInstanceState == null) {
            mAppFragManager.beginTransaction()
                    .replace(R.id.container_main, ListTabLayout.newInstance(), FRAG_PAGER_TAG)
                    .commit();
        }

        mMainContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mMainContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                mUpFab.hide();
                return true;
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (mDataProvider != null) mDataProvider.dispose();
        if (mUnbinder != null) mUnbinder.unbind();
        super.onDestroy();
    }

    //Solution to fragment backPressed listener by Hw.Master
    // https://stackoverflow.com/questions/5448653/how-to-implement-onbackpressed-in-fragments
    @Override
    public void onBackPressed() {

        int fragCount = mAppFragManager.getBackStackEntryCount();

        if (fragCount == 0) {
            super.onBackPressed();
        } else {
            mAppFragManager.popBackStack();
        }

    }

    @Override
    public void onRecyclerViewScrolled(ScrollDirection scrollDirection) {
        if (scrollDirection == ScrollDirection.SCROLL_DOWN &&
                mUpFab.getVisibility() == View.VISIBLE) mUpFab.hide();
        else if (scrollDirection == ScrollDirection.SCROLL_UP &&
                mUpFab.getVisibility() != View.VISIBLE) mUpFab.show();
    }

    @Override
    public void favFabClicked(Movie movie, Boolean isFavourite) {
        if (isFavourite) mDataProvider.deleteFromFavourites(movie);
        else mDataProvider.addToFavourites(movie);
    }

    @Override
    public void onPageDrag(int state) {
        if (state == ViewPager.SCROLL_STATE_DRAGGING && mUpFab.getVisibility() == View.VISIBLE) mUpFab.hide();
        else if (state == ViewPager.SCROLL_STATE_IDLE && mUpFab.getVisibility() != View.VISIBLE) mUpFab.show();
    }

    @Override
    public void onClick(View holder, Movie movie, String transitionIdentifier) {
        mDataProvider.setMovieAndFetch(movie);

        View poster = holder.findViewById(R.id.iv_poster);
        View posterWrapper = holder.findViewById(R.id.cv_poster_wrapper);
        View bg = holder.findViewById(R.id.cv_view_holder_wrapper);

        MovieDetailsFragment newFrag = MovieDetailsFragment.newInstance(movie,
                mFavListVM.checkIfFavourite(movie.getMovieId()), transitionIdentifier);

        Fragment currFrag = mAppFragManager.findFragmentById(R.id.container_main);

        currFrag.setExitTransition(new Fade());

        newFrag.setSharedElementEnterTransition(TransitionInflater.from(this)
                .inflateTransition(android.R.transition.move));

        mUpFab.hide();

        mAppFragManager.beginTransaction()
                .addSharedElement(poster, ViewCompat.getTransitionName(poster))
                .addSharedElement(posterWrapper, ViewCompat.getTransitionName(posterWrapper))
                .addSharedElement(bg, ViewCompat.getTransitionName(bg))
                .addToBackStack(null)
                .replace(R.id.container_main, newFrag, DETAIL_FRAGMENT_TAG)
                .commit();

        holder.findViewById(R.id.v_card_shadow).setVisibility(View.INVISIBLE);

    }

    /*
    Resources/Tutorials for this project:
    - FragmentTransitions:
        https://medium.com/workday-engineering/android-inbox-material-transitions-for-recyclerview-7ae3cb241aed
        https://medium.com/bynder-tech/how-to-use-material-transitions-in-fragment-transactions-5a62b9d0b26b
        https://www.androiddesignpatterns.com/2014/12/activity-fragment-transitions-in-android-lollipop-part1.html
        http://mikescamell.com/shared-element-transitions-part-4-recyclerview/
        https://developer.android.com/training/animation/reveal-or-hide-view
     */

}


