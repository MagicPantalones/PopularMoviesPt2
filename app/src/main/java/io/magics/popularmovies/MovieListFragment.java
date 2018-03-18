package io.magics.popularmovies;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.ThreadingUtils;

import static io.magics.popularmovies.networkutils.ApiUtils.*;
import static io.magics.popularmovies.networkutils.TMDBApi.*;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MovieListFragment}.OnFavouriteMovieSelected} interface
 * to handle interaction events.
 * Use the {@link MovieListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieListFragment extends Fragment
implements PosterAdapter.PosterClickHandler, FragmentListTabLayout.UpFabListener{
    private static final String ARG_TAB_PAGE = "ARG_TAB_PAGE";
    private static final String ARG_POPULAR_PAGE = "ARG_POP_PAGE";
    private static final String ARG_TOP_RATED_PAGE = "ARG_TOP_PAGE";

    private int mTabPage = 1;
    private int mPopPage = 1;
    private int mTopPage = 1;

    @BindView(R.id.rv_poster_list) RecyclerView mRvPoster;
    @BindView(R.id.tv_error) TextView mTvError;
    private Unbinder unbinder;
    private PosterAdapter mAdapter;


    public static MovieListFragment newInstance(int page) {
        MovieListFragment fragment = new MovieListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_PAGE, page);
        args.putInt(ARG_POPULAR_PAGE, 1);
        args.putInt(ARG_TOP_RATED_PAGE, 1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTabPage = getArguments().getInt(ARG_TAB_PAGE);
            mPopPage = getArguments().getInt(ARG_POPULAR_PAGE);
            mTopPage = getArguments().getInt(ARG_TOP_RATED_PAGE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = getContext();
        Boolean getDataSuccess;
        View rootView = inflater.inflate(R.layout.fragment_list_movie_lists, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
        //noinspection ConstantConditions
        FloatingActionButton mainUpFab = ((FragmentListTabLayout)this.getParentFragment()).mUpFab;
        if (mAdapter == null){
            mAdapter = new PosterAdapter(this);
        }

        rootView.setPaddingRelative(16, 16, 16, 16);

        //From Tara's answer here: https://stackoverflow.com/questions/2680607/text-with-gradient-in-android
        paintTextView(context, mTvError);

        getDataSuccess = getDataFromNetwork(context, mAdapter);

        if (getDataSuccess) {
            if (mTabPage == 1) {
                mAdapter.setEndListener(handler -> {
                    mTopPage += 1;
                    getDataFromNetwork(context, mAdapter);
                });
            } else if (mTabPage == 2) {
                mAdapter.setEndListener(handler -> {
                    mPopPage += 1;
                    getDataFromNetwork(context, mAdapter);
                });
            }
        }

        mRvPoster.setVisibility(getDataSuccess ? View.VISIBLE : View.GONE);
        mTvError.setVisibility(getDataSuccess ? View.GONE : View.VISIBLE);

        mRvPoster.setAdapter(mAdapter);
        mRvPoster.setLayoutManager(layoutManager);

        mRvPoster.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) mainUpFab.hide();
                if (dy <= 0) mainUpFab.show();
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        //noinspection ConstantConditions
        ((FragmentListTabLayout)this.getParentFragment()).registerUpFab(this);
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        //noinspection ConstantConditions
        ((FragmentListTabLayout)this.getParentFragment()).unRegisterUpFab(this);
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onClick(Movie movie, int position) {
        //noinspection ConstantConditions
        ((MovieListsActivity)getContext()).showMovieDetailsFrag(movie, false);
    }

    private boolean getDataFromNetwork(Context context, PosterAdapter posterAdapter){
        SortingMethod sortingMethod;
        int pageNumber;
        if (mTabPage == 3){
            ThreadingUtils.queryForFavouriteMovies(context, result ->
            posterAdapter.setMovieData(result, posterAdapter.getItemCount(), true));
            return true;
        }

        if (!isConnected(context)) return false;

        if (mTabPage == 1) {
                sortingMethod = SortingMethod.TOP_RATED;
                pageNumber = mTopPage;
        } else {
                sortingMethod = SortingMethod.POPULAR;
                pageNumber = mPopPage;
        }

        callApiForMovieList(sortingMethod,
                pageNumber,
                apiResult -> posterAdapter.setMovieData(apiResult.getMovies(), posterAdapter.getItemCount(),false));
        return true;
    }



    private void paintTextView(Context context, TextView textView){
        int colorOne = context.getResources().getColor(R.color.colorSecondaryLight);
        int colorTwo = context.getResources().getColor(R.color.colorSecondaryAccent);

        Shader shader = new LinearGradient(0, 0, 0, 45,
                new int[]{colorOne, colorTwo},
                new float[]{0,1}, Shader.TileMode.REPEAT);
        textView.getPaint().setShader(shader);
    }

    @Override
    public void upFabUp() {
        mRvPoster.smoothScrollToPosition(0);
    }
}
