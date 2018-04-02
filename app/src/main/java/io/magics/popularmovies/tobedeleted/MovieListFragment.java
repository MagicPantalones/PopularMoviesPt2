package io.magics.popularmovies.tobedeleted;

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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.MovieListsActivity;
import io.magics.popularmovies.R;
import io.magics.popularmovies.fragments.listfragments.ListAdapter;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.MovieUtils;

import static io.magics.popularmovies.utils.MovieUtils.hideAndShowView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MovieListFragment}.OnFavouriteMovieSelected} interface
 * to handle interaction events.
 * Use the {@link MovieListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieListFragment extends Fragment
        implements ListAdapter.PosterClickHandler, FragmentListTabLayout.UpFabListener {
    private static final String ARG_TAB_PAGE = "ARG_TAB_PAGE";

    private int mTabPage = 1;

    @BindView(R.id.rv_poster_list)
    RecyclerView mRvPoster;
    @BindView(R.id.tv_error)
    TextView mTvError;
    @BindView(R.id.tv_no_fav)
    TextView mTvNoFav;
    private Unbinder unbinder;
    private ListAdapter mAdapter;


    public static MovieListFragment newInstance(int page) {
        MovieListFragment fragment = new MovieListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTabPage = getArguments().getInt(ARG_TAB_PAGE);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = getContext();
        View rootView = inflater.inflate(R.layout.fragment_list_movie_lists, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
        //noinspection ConstantConditions
        FloatingActionButton mainUpFab = ((FragmentListTabLayout) this.getParentFragment()).mUpFab;
        MovieListsActivity parentActivity = (MovieListsActivity) getContext();

        if (mAdapter == null) {
            mAdapter = new ListAdapter(this);
            if (mTabPage == 1) parentActivity.registerTopListener(this);
            if (mTabPage == 2) parentActivity.registerPopListener(this);
            if (mTabPage == 3){
                MovieUtils.hideAndShowView(mTvNoFav, mRvPoster);
                parentActivity.registerFavListener(this);
            }
        }

        if (mTabPage == 3) parentActivity.notifyReadyForFav();

        rootView.setPaddingRelative(16, 16, 16, 16);

        //From Tara's answer here: https://stackoverflow.com/questions/2680607/text-with-gradient-in-android
        paintTextView(context, mTvError);
        paintTextView(context, mTvNoFav);

        //noinspection ConstantConditions
        if (!((MovieListsActivity) getContext()).hasRequestedFromNetwork() && (mTabPage != 3)) {
            hideAndShowView(mTvError, mRvPoster);
        }

        if (mTabPage == 1) {
            mAdapter.setEndListener(handler ->
                    parentActivity.getMoreTopRated());
        }

        if (mTabPage == 2) {
            mAdapter.setEndListener(handler ->
                    parentActivity.getMorePopular());
        }

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

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onAttach(Context context) {
        ((FragmentListTabLayout) this.getParentFragment()).registerUpFab(this);
        super.onAttach(context);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onDetach() {
        MovieListsActivity parentActivity = (MovieListsActivity) getContext();
        ((FragmentListTabLayout) this.getParentFragment()).unRegisterUpFab(this);

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
        ((MovieListsActivity) getContext()).showMovieDetailsFrag(movie);
    }


    private void paintTextView(Context context, TextView textView) {
        int colorOne = context.getResources().getColor(R.color.colorSecondaryLight);
        int colorTwo = context.getResources().getColor(R.color.colorSecondaryAccent);

        Shader shader = new LinearGradient(90, 0, 0, 0,
                new int[]{colorOne, colorTwo},
                new float[]{0, 1}, Shader.TileMode.MIRROR);
        textView.getPaint().setShader(shader);
    }

    @Override
    public void upFabUp() { mRvPoster.smoothScrollToPosition(0); }

    @Override
    public void topMoviesResultDelivery(List<Movie> movies) {
        if (mTabPage == 1) mAdapter.setMovieData(movies, mAdapter.getItemCount());

    }

    @Override
    public void popularMoviesDelivery(List<Movie> movies) {
        if (mTabPage == 2) mAdapter.setMovieData(movies, mAdapter.getItemCount());
    }

    @Override
    public void favouritesResultDelivery(List<Movie> movies) {
        if (mTabPage == 3) {
            mAdapter = new ListAdapter(this);
            mRvPoster.setAdapter(mAdapter);
            if (movies.isEmpty()) MovieUtils.hideAndShowView(mTvNoFav, mRvPoster);
            else MovieUtils.hideAndShowView(mRvPoster, mTvNoFav);
            mAdapter.setMovieData(movies, 0);
        }
    }
}
