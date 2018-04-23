package io.magics.popularmovies.fragments.listfragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.MovieUtils;
import io.magics.popularmovies.utils.MovieUtils.ScrollDirection;
import io.magics.popularmovies.viewmodels.TopListViewModel;


@SuppressWarnings("ConstantConditions")
public class ListTopRatedFragment extends Fragment
    implements ListAdapter.PosterClickHandler{

    @BindView(R.id.rv_top_rated_list)
    RecyclerView mRvTopRated;
    @BindView(R.id.tv_error_top_rated)
    TextView mTvTopRated;

    ListAdapter mAdapter;
    GridLayoutManager mGridManager;
    TopListViewModel mViewModel;

    TopRatedFragmentListener mFragmentListener;
    Unbinder mUnbinder;


    public interface TopRatedFragmentListener{
        void showClickedTopMovie(Movie movie);
        void topRatedRvScrolled(ScrollDirection scrollDirection);
    }

    public ListTopRatedFragment() {
        // Required empty public constructor
    }


    public static ListTopRatedFragment newInstance() {
        return new ListTopRatedFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list_top_rated, container, false);
        mUnbinder = ButterKnife.bind(this, root);
        mViewModel = ViewModelProviders.of(getActivity()).get(TopListViewModel.class);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new ListAdapter(this, mViewModel);
        mGridManager = new GridLayoutManager(getContext(), 2);

        mRvTopRated.setLayoutManager(mGridManager);
        mRvTopRated.setAdapter(mAdapter);

        mRvTopRated.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mFragmentListener != null) {
                    if (dy > 0) mFragmentListener.topRatedRvScrolled(ScrollDirection.SCROLL_DOWN);
                    if (dy <= 0) mFragmentListener.topRatedRvScrolled(ScrollDirection.SCROLL_UP);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        mViewModel.mTopList.observe(getActivity(), movies -> {
            if (movies == null || movies.isEmpty()) MovieUtils.toggleViewVisibility(mTvTopRated, mRvTopRated);
            else mAdapter.setMovieData(movies);
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TopRatedFragmentListener) {
            mFragmentListener = (TopRatedFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        if (mUnbinder != null) mUnbinder.unbind();
        super.onDetach();
    }

    @Override
    public void onClick(Movie movie) {
        if (mFragmentListener != null) mFragmentListener.showClickedTopMovie(movie);
    }

    public void scrollTopListToZero(){ mRvTopRated.smoothScrollToPosition(0); }
}
