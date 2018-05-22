package io.magics.popularmovies.fragments.listfragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.MovieListsActivity;
import io.magics.popularmovies.R;
import io.magics.popularmovies.fragments.detailfragments.MovieDetailsFragment;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.MovieUtils.ScrollDirection;
import io.magics.popularmovies.viewmodels.FavListViewModel;
import io.magics.popularmovies.viewmodels.PopListViewModel;
import io.magics.popularmovies.viewmodels.TopListViewModel;

import static io.magics.popularmovies.utils.MovieUtils.toggleViewVisibility;

@SuppressWarnings("ConstantConditions")
public class ListFragment extends Fragment {

    public static final int TOP_FRAGMENT = 0;
    public static final int POP_FRAGMENT = 1;
    public static final int FAV_FRAGMENT = 2;

    private static final String ARG_FRAGMENT_TYPE = "mFragType";

    private static final String KEY_OLD_RIGHT = "oldRight";

    private int mFragType;

    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_list_error)
    TextView mTvError;

    private Unbinder mUnbinder;

    private ListAdapter mAdapter;

    private TopListViewModel mTopVm;
    private PopListViewModel mPopVm;
    private FavListViewModel mFavVm;

    private FragmentListener mListener;

    private int mOldRight;

    public ListFragment() {
        // Required empty public constructor
    }

    public static ListFragment newInstance(int fragType) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FRAGMENT_TYPE, fragType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFragType = getArguments().getInt(ARG_FRAGMENT_TYPE);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        //Initializing the screen rotation check for the OnLayoutChanged listener
        if (savedInstanceState == null) {
            mOldRight = -1;
        } else {
            mOldRight = savedInstanceState.getInt(KEY_OLD_RIGHT);
        }

        switch (mFragType) {
            case 0:
                mTopVm = ViewModelProviders.of(getActivity()).get(TopListViewModel.class);
                if (mAdapter != null) break;
                mAdapter = new ListAdapter((ListAdapter.ListItemEventHandler) getContext(), mTopVm,
                        mFragType);
                break;
            case 1:
                mPopVm = ViewModelProviders.of(getActivity()).get(PopListViewModel.class);
                if (mAdapter != null) break;
                mAdapter = new ListAdapter((ListAdapter.ListItemEventHandler) getContext(), mPopVm,
                        mFragType);
                break;
            case 2:
                mFavVm = ViewModelProviders.of(getActivity()).get(FavListViewModel.class);
                if (mAdapter != null) break;
                mAdapter = new ListAdapter((ListAdapter.ListItemEventHandler) getContext());
                break;
            default:
                //should never happen
                break;
        }


        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //See #correctScroll
        correctScroll();

        mRecyclerView.setAdapter(mAdapter);

        //Prepares the GridLayoutManager for a horizontal layout
        if (getContext().getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager manager = (GridLayoutManager) mRecyclerView.getLayoutManager();
            manager.setSpanCount(1);
            manager.setOrientation(GridLayoutManager.HORIZONTAL);
        }

        /*To get the wanted behaviour of the up navigation FAB. It communicates when the
          RecyclerView is scrolled */
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mListener == null) mListener = (FragmentListener) getContext();
                if (dy > 0) mListener.onRecyclerViewScrolled(ScrollDirection.SCROLL_DOWN);
                if (dy <= 0) mListener.onRecyclerViewScrolled(ScrollDirection.SCROLL_UP);
            }
        });

        /* Chose to use LiveData instead of RxJava in my ViewModels.
           LiveData had a lot more documentation for use in ViewModel classes */
        Observer<List<Movie>> movieObserver = movies -> {
            if (movies == null || movies.isEmpty()) toggleViewVisibility(mRecyclerView, mTvError);
            else mAdapter.setMovieData(movies);
        };

        switch (mFragType) {
            case 0:
                mTopVm.mTopList.observe(getActivity(), movieObserver);
                break;
            case 1:
                mPopVm.mPopList.observe(getActivity(), movieObserver);
                break;
            case 2:
                mFavVm.mFavList.observe(getActivity(), movieObserver);
                break;
            default:
                //Should never happen
                break;
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //Saves the right coordinates of the layout.
        outState.putInt(KEY_OLD_RIGHT, mOldRight);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener) {
            mListener = (FragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //Don't know if I have to null check the Unbinder, but better safe than sorry.
        if (mUnbinder != null) mUnbinder.unbind();
        mListener = null;
    }

    public void scrollRecyclerViewToTop() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    /**
     * From <a href="https://github.com/google/android-transition-examples/tree/master/GridToPager">
     *     https://github.com/google/android-transition-examples/tree/master/GridToPager</a>
     *     <br></br>
     *     <br></br>
     * Since my RecyclerView's ViewHolders would clip the appbar when exiting.<br></br>
     * This method will set an OnLayoutChanged listener that will adjust the RecyclerView's
     * LayoutManagerPosition to show the full ViewHolder when navigating back to the list from a
     * detail fragment.
     * <br></br>
     * <br></br>
     * The listener will not adjust the position if the device is rotated since the
     * {@link MovieListsActivity#getSelectedPosition} return value beeing static and not set until
     * after navigating away from the detail fragment.
     * <br></br>
     * <br></br>
     * If you know of a prettier or better way to do this, please let me know :)
     */

    private void correctScroll() {
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mRecyclerView.removeOnLayoutChangeListener(this);
                if (mOldRight != -1 || mOldRight == right) return;

                final MovieDetailsFragment shownFrag = (MovieDetailsFragment) getActivity()
                        .getSupportFragmentManager()
                        .findFragmentByTag(MovieListsActivity.DETAIL_FRAGMENT_TAG);

                if (shownFrag == null || shownFrag.getParentListType() != mFragType) return;
                final RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
                View viewAtPos = manager.findViewByPosition(MovieListsActivity.getSelectedPosition());

                if (viewAtPos == null || manager.isViewPartiallyVisible(viewAtPos,
                        false, true)) {
                    mRecyclerView.post(() -> {
                        manager.scrollToPosition(MovieListsActivity.getSelectedPosition());
                        mOldRight = right;
                    });
                }
            }

        });


    }

    public interface FragmentListener {
        void onRecyclerViewScrolled(ScrollDirection scrollDirection);
    }

}
