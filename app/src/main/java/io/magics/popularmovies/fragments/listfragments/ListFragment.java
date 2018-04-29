package io.magics.popularmovies.fragments.listfragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.MovieUtils;
import io.magics.popularmovies.utils.MovieUtils.ScrollDirection;
import io.magics.popularmovies.viewmodels.FavListViewModel;
import io.magics.popularmovies.viewmodels.PopListViewModel;
import io.magics.popularmovies.viewmodels.TopListViewModel;

import static io.magics.popularmovies.utils.MovieUtils.toggleViewVisibility;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentListener} interface
 * to handle interaction events.
 * Use the {@link ListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment implements ListAdapter.PosterClickHandler{

    public static final int TOP_FRAGMENT = 0;
    public static final int POP_FRAGMENT = 1;
    public static final int FAV_FRAGMENT = 2;

    private static final String ARG_FRAGMENT_TYPE = "mFragType";

    private int mFragType;

    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_list_error)
    TextView mTvError;

    Unbinder mUnbinder;

    private ListAdapter mAdapter;
    private GridLayoutManager mGridManager;

    private TopListViewModel mTopVm;
    private PopListViewModel mPopVm;
    private FavListViewModel mFavVm;

    private FragmentListener mListener;

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

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        switch (mFragType){
            case 0:
                mTopVm = ViewModelProviders.of(getActivity()).get(TopListViewModel.class);
                mAdapter = new ListAdapter(this, mTopVm);
                break;
            case 1:
                mPopVm = ViewModelProviders.of(getActivity()).get(PopListViewModel.class);
                mAdapter = new ListAdapter(this, mPopVm);
                break;
            case 2:
                mFavVm = ViewModelProviders.of(getActivity()).get(FavListViewModel.class);
                mAdapter = new ListAdapter(this);
                break;
            default:
                //should never happen
                break;
        }

        return root;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGridManager = new GridLayoutManager(getContext(), 2);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mGridManager);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mListener == null) mListener = (FragmentListener) getContext();
                if (dy > 0) mListener.onRecyclerViewScrolled(ScrollDirection.SCROLL_DOWN);
                if (dy <= 0) mListener.onRecyclerViewScrolled(ScrollDirection.SCROLL_UP);
            }
        });

        Observer<List<Movie>> movieObserver = movies -> {
            if (movies == null || movies.isEmpty()) toggleViewVisibility(mRecyclerView, mTvError);
            else mAdapter.setMovieData(movies);
        };

        switch (mFragType){
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener) {
            mListener = (FragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mUnbinder != null) mUnbinder.unbind();
        mListener = null;
    }

    public void scrollRecyclerViewToTop(){ mRecyclerView.smoothScrollToPosition(0); }

    public void setSavedAdapterData(List<Movie> movies, int adapterPosition, int adapterOffset){
        mAdapter.setMovieData(movies);
        mRecyclerView.setAdapter(mAdapter);
        mGridManager.scrollToPositionWithOffset(adapterPosition, adapterOffset);
    }

    @Override
    public void onClick(View v, Movie movie) {
        if (mListener == null) mListener = (FragmentListener) getContext();
        //noinspection ConstantConditions
        mListener.onMovieViewHolderClicked(mRecyclerView, v, movie);
    }

    public interface FragmentListener {
        void onMovieViewHolderClicked(RecyclerView recycler, View v, Movie movie);
        void onRecyclerViewScrolled(ScrollDirection scrollDirection);
    }
}