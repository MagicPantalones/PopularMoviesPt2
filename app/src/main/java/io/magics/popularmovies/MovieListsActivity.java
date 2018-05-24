package io.magics.popularmovies;

import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Configuration;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.fragments.detailfragments.MovieDetailsFragment;
import io.magics.popularmovies.fragments.listfragments.ListAdapter;
import io.magics.popularmovies.fragments.listfragments.ListFragment;
import io.magics.popularmovies.fragments.listfragments.ListTabLayout;
import io.magics.popularmovies.models.ApiResult;
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
        ListAdapter.ListItemEventHandler {

    private static final String FRAG_PAGER_TAG = "pagerTag";
    public static final String DETAIL_FRAGMENT_TAG = "detailFrag";

    private static final String KEY_VM_STATE = "vmState";

    private static int selectedPosition;

    @BindView(R.id.up_fab)
    FloatingActionButton mUpFab;

    private DataProvider mDataProvider;

    private TopListViewModel mTopListVm;
    private PopListViewModel mPopListVm;
    private FavListViewModel mFavListVM;

    private Unbinder mUnbinder;

    private FragmentManager mAppFragManager;

    /**
     * Creates and initialize the {@link DataProvider}, {@link TopListViewModel},
     * {@link PopListViewModel}, {@link FavListViewModel}, {@link TrailersViewModel} and
     * {@link ReviewsViewModel} that fetches and stores the API data from TheMovieDatabase.
     *
     * @see DataProvider DataProvider
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_lists);

        mUnbinder = ButterKnife.bind(this);

        mTopListVm = ViewModelProviders.of(this).get(TopListViewModel.class);
        mPopListVm = ViewModelProviders.of(this).get(PopListViewModel.class);
        mFavListVM = ViewModelProviders.of(this).get(FavListViewModel.class);

        TrailersViewModel trailerVm = ViewModelProviders.of(this).get(TrailersViewModel.class);
        ReviewsViewModel reviewVm = ViewModelProviders.of(this).get(ReviewsViewModel.class);

        mAppFragManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            //A ViewHolder has not been selected yet so it sets the position to 0.
            mDataProvider = new DataProvider(this, mTopListVm, mPopListVm, mFavListVM,
                    trailerVm, reviewVm);
            setSelectedPosition(0);
            mAppFragManager.beginTransaction()
                    .replace(android.R.id.content, ListTabLayout.newInstance(), FRAG_PAGER_TAG)
                    .commit();

        } else {
            List<ApiResult> state = savedInstanceState.getParcelableArrayList(KEY_VM_STATE);
            if (state != null) {
                mDataProvider = new DataProvider(this, mTopListVm, mPopListVm, mFavListVM,
                        trailerVm, reviewVm, state);
            }
        }

        mDataProvider.initialiseApp();

        mUpFab.setOnClickListener(v -> {
            ListTabLayout tabFrag = (ListTabLayout) mAppFragManager
                    .findFragmentByTag(FRAG_PAGER_TAG);
            if (tabFrag != null) tabFrag.notifyUpFabPressed();
        });

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mUpFab.setRotation(-90);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Have to hide the UpFab in onResume to avoid it showing if the device is rotated when a
        //detail fragment is showing.
        mUpFab.hide();
    }

    @Override
    protected void onDestroy() {
        if (mDataProvider != null) mDataProvider.dispose();
        if (mUnbinder != null) mUnbinder.unbind();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        List<ApiResult> apiResults = new ArrayList<>();
        apiResults.add(mTopListVm.getPageStates());
        apiResults.add(mPopListVm.getPageStates());
        outState.putParcelableArrayList(KEY_VM_STATE, (ArrayList<ApiResult>) apiResults);
    }

    //Solution to fragment backPressed listener by Hw.Master
    // https://stackoverflow.com/questions/5448653/how-to-implement-onbackpressed-in-fragments
    @Override
    public void onBackPressed() {

        int fragCount = mAppFragManager.getBackStackEntryCount();

        if (fragCount == 0) {
            super.onBackPressed();
        } else {
            MovieDetailsFragment frag = (MovieDetailsFragment)
                    mAppFragManager.findFragmentByTag(DETAIL_FRAGMENT_TAG);

            if (frag != null) {
                frag.prepareToGetPopped();
            }

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
    public void onRefreshRequest(int listType) {
        mDataProvider.refreshList(listType);
    }

    @Override
    public void favFabClicked(Movie movie, Boolean isFavourite) {
        if (isFavourite) mDataProvider.deleteFromFavourites(movie);
        else mDataProvider.addToFavourites(movie);
    }

    @Override
    public void onPageDrag(int state) {
        if (state == ViewPager.SCROLL_STATE_DRAGGING && mUpFab.getVisibility() == View.VISIBLE)
            mUpFab.hide();
        else if (state == ViewPager.SCROLL_STATE_IDLE && mUpFab.getVisibility() != View.VISIBLE)
            mUpFab.show();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onClick(View holder, Movie movie, int transitionIdentifier, int selectedPos) {
        mDataProvider.setMovieAndFetch(movie);

        setSelectedPosition(selectedPos);

        ListTabLayout currTabLayout = (ListTabLayout)
                mAppFragManager.findFragmentByTag(FRAG_PAGER_TAG);

        MovieDetailsFragment newFrag = MovieDetailsFragment.newInstance(movie,
                mFavListVM.checkIfFavourite(movie.getMovieId()), transitionIdentifier);

        View posterWrapper = holder.findViewById(R.id.cv_poster_wrapper);
        View poster = holder.findViewById(R.id.iv_poster);
        //Found a suggested solution to use the AppBar as a shared element, but had no success.
        //Have not removed yet, because I think I'm on the right path and intend to figure it out.
        View toolBar = currTabLayout.getView().findViewById(R.id.app_bar_tab_layout);

        mUpFab.hide();

        mAppFragManager.beginTransaction()
                .setReorderingAllowed(true)
                .addSharedElement(posterWrapper, posterWrapper.getTransitionName())
                .addSharedElement(poster, poster.getTransitionName())
                .addSharedElement(toolBar, toolBar.getTransitionName())
                .replace(android.R.id.content, newFrag, DETAIL_FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onImageLoaded(int adapterPosition) {
        ListTabLayout tabFrag = (ListTabLayout) mAppFragManager.findFragmentByTag(FRAG_PAGER_TAG);

        if (adapterPosition == selectedPosition) {
            tabFrag.startPostponedEnterTransition();
        }
    }

    public static int getSelectedPosition() { return selectedPosition; }

    public static void setSelectedPosition(int position) { selectedPosition = position; }

    /*
    Activity/Fragment Transition Resources/Tutorials for this project:
    - Used:
        http://mikescamell.com/shared-element-transitions-part-4-recyclerview/
        https://github.com/google/android-transition-examples/tree/master/GridToPager

    - Saved for later:
        https://medium.com/workday-engineering/android-inbox-material-transitions-for-recyclerview-7ae3cb241aed
        https://medium.com/bynder-tech/how-to-use-material-transitions-in-fragment-transactions-5a62b9d0b26b
        https://www.androiddesignpatterns.com/2014/12/activity-fragment-transitions-in-android-lollipop-part1.html
        https://developer.android.com/training/animation/reveal-or-hide-view
     */

}


