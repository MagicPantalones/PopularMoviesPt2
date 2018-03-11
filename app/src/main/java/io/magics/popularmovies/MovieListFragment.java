package io.magics.popularmovies;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.ThreadingUtils;

import static io.magics.popularmovies.networkutils.ApiUtils.*;
import static io.magics.popularmovies.networkutils.TMDBApi.*;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MovieListFragment}.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MovieListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieListFragment extends Fragment
implements PosterAdapter.PosterClickHandler{
    private static final String ARG_TAB_PAGE = "ARG_TAB_PAGE";
    private static final String ARG_POPULAR_PAGE = "ARG_POP_PAGE";
    private static final String ARG_TOP_RATED_PAGE = "ARG_TOP_PAGE";

    private int mTabPage = 1;
    private int mPopPage = 1;
    private int mTopPage = 1;

    public MovieListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param page tab page number
     * @return A new instance of fragment MovieListFragment.
     */

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
        View rootView = inflater.inflate(R.layout.movie_list_fragment, container, false);
        RecyclerView rv = rootView.findViewById(R.id.rv_poster_list);
        TextView tv = rootView.findViewById(R.id.iv_error);
        PosterAdapter adapter = new PosterAdapter(this);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 2);

        rootView.setPaddingRelative(16, 16, 16, 16);

        //From Tara's answer here: https://stackoverflow.com/questions/2680607/text-with-gradient-in-android
        paintTextView(context, tv);

        getDataSuccess = getDataFromNetwork(context, adapter);

        if (getDataSuccess) {
            if (mTabPage == 1) {
                adapter.setEndListener(handler -> {
                    mTopPage += 1;
                    getDataFromNetwork(context, adapter);
                });
            } else if (mTabPage == 2) {
                adapter.setEndListener(handler -> {
                    mPopPage += 1;
                    getDataFromNetwork(context, adapter);
                });
            }
        }

        rv.setVisibility(getDataSuccess ? View.VISIBLE : View.GONE);
        tv.setVisibility(getDataSuccess ? View.GONE : View.VISIBLE);

        rv.setAdapter(adapter);
        rv.setLayoutManager(layoutManager);

        return rootView;

    }

    @Override
    public void onClick(Movie movie, View view) {
        //TODO Implement details fragment.
    }

    private boolean getDataFromNetwork(Context context, PosterAdapter posterAdapter){
        SortingMethod sortingMethod;
        int pageNumber;
        if (mTabPage == 3){
            ThreadingUtils.queryFavouritesCursor(context, result ->
            posterAdapter.setMovieData(result, true));
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

        callApi(sortingMethod,
                pageNumber,
                apiResult -> posterAdapter.setMovieData(apiResult.getMovies(), false));
        return true;
    }

    private void paintTextView(Context context, TextView textView){
        int colorOne = context.getResources().getColor(R.color.colorSecondaryLight);
        int colorTwo = context.getResources().getColor(R.color.colorSecondaryAccent);

        Shader shader = new LinearGradient(0, 0, 0, 45,
                new int[]{colorOne, colorTwo},
                new float[]{0,1}, Shader.TileMode.CLAMP);
        textView.getPaint().setShader(shader);
    }

}
