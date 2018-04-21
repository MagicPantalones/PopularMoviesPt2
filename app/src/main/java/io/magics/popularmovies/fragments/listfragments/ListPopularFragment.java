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
import io.magics.popularmovies.viewmodels.PopListViewModel;

@SuppressWarnings("ConstantConditions")
public class ListPopularFragment extends Fragment
    implements ListAdapter.PosterClickHandler{

    @BindView(R.id.rv_popular_list)
    RecyclerView mRvPopular;
    @BindView(R.id.tv_error_popular)
    TextView mTvPopularError;

    ListAdapter mAdapter;
    GridLayoutManager mGridManager;
    PopListViewModel mViewModel;

    PopularFragmentListener mFragmentListener;
    Unbinder mUnbinder;


    public interface PopularFragmentListener{
        void showClickedPopMovie(Movie movie);
        void popularRvScrolled(ScrollDirection scrollDirection);
    }

    public ListPopularFragment() {
        // Required empty public constructor
    }

    public static ListPopularFragment newInstance() {
        return new ListPopularFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list_popular, container, false);
        mUnbinder = ButterKnife.bind(this, root);
        mViewModel = ViewModelProviders.of(getActivity()).get(PopListViewModel.class);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new ListAdapter(this, mViewModel);
        mGridManager = new GridLayoutManager(getContext(), 2);

        mRvPopular.setLayoutManager(mGridManager);
        mRvPopular.setAdapter(mAdapter);

        mRvPopular.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mFragmentListener != null) {
                    if (dy > 0) mFragmentListener.popularRvScrolled(ScrollDirection.SCROLL_DOWN);
                    if (dy <= 0) mFragmentListener.popularRvScrolled(ScrollDirection.SCROLL_UP);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        mViewModel.mPopList.observe(getActivity(), movies -> {
            if (movies == null || movies.isEmpty()) MovieUtils.showAndHideViews(mTvPopularError, mRvPopular);
            else mAdapter.setMovieData(movies);
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PopularFragmentListener){
            mFragmentListener = (PopularFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        if (mUnbinder != null) mUnbinder.unbind();
        super.onDetach();
    }


    @Override
    public void onClick(Movie movie) {
        if (mFragmentListener != null) mFragmentListener.showClickedPopMovie(movie);
    }

    public void scrollPopListToZero(){ mRvPopular.smoothScrollToPosition(0); }

}
