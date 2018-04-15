package io.magics.popularmovies.fragments.listfragments;

import android.arch.lifecycle.ViewModelProviders;
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

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.MovieUtils;
import io.magics.popularmovies.utils.MovieUtils.ScrollDirection;
import io.magics.popularmovies.viewmodels.FavListViewModel;


public class ListFavouritesFragment extends Fragment
        implements ListAdapter.PosterClickHandler{

    @BindView(R.id.rv_favourites_list)
    RecyclerView mRvFavourites;
    @BindView(R.id.tv_no_fav)
    TextView mTvNoFavourites;

    ListAdapter mAdapter;
    GridLayoutManager mGridManager;
    FavListViewModel mViewModel;

    FavouritesFragmentListener mFragmentListener;
    Unbinder mUnbinder;

    public interface FavouritesFragmentListener{
        void showClickedFavMovie(Movie movie);
        void favouriteRvScrolled(ScrollDirection scrollDirection);
    }

    public ListFavouritesFragment() {
        // Required empty public constructor
    }

    public static ListFavouritesFragment newInstance() {
        return new ListFavouritesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list_top_rated, container,
                false);
        mUnbinder = ButterKnife.bind(this, root);

        //noinspection ConstantConditions
        mViewModel = ViewModelProviders.of(getActivity())
                .get(FavListViewModel.class);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new ListAdapter(this);
        mGridManager = new GridLayoutManager(getContext(), 2);

        mRvFavourites.setLayoutManager(mGridManager);
        mRvFavourites.setAdapter(mAdapter);

        mRvFavourites.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mFragmentListener != null) {
                    if (dy > 0) mFragmentListener.favouriteRvScrolled(ScrollDirection.SCROLL_DOWN);
                    if (dy <= 0) mFragmentListener.favouriteRvScrolled(ScrollDirection.SCROLL_UP);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        //noinspection ConstantConditions
        mViewModel.mFavList.observe(getActivity(), movies -> {
            if (movies == null || movies.isEmpty()) MovieUtils.hideAndShowView(mTvNoFavourites,
                    mRvFavourites);
            else mAdapter.setMovieData(movies, mAdapter.getItemCount());
        });
    }

    @Override
    public void onDetach() {
        if (mUnbinder != null) mUnbinder.unbind();
        super.onDetach();
    }

    @Override
    public void onClick(Movie movie) {
        if (mFragmentListener != null) mFragmentListener.showClickedFavMovie(movie);
    }

    public void scrollFavListToZero(){ mRvFavourites.smoothScrollToPosition(0); }

}
