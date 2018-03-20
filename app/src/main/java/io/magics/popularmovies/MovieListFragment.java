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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.MovieListsActivity.MovieResultType;
import io.magics.popularmovies.models.Movie;

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
        implements PosterAdapter.PosterClickHandler, FragmentListTabLayout.UpFabListener,
        MovieListsActivity.MovieResultsListener {
    private static final String ARG_TAB_PAGE = "ARG_TAB_PAGE";

    private int mTabPage = 1;

    @BindView(R.id.rv_poster_list)
    RecyclerView mRvPoster;
    @BindView(R.id.tv_error)
    TextView mTvError;
    @BindView(R.id.tv_no_fav)
    TextView mTvNoFav;
    private Unbinder unbinder;
    private PosterAdapter mAdapter;


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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = getContext();
        View rootView = inflater.inflate(R.layout.fragment_list_movie_lists, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
        //noinspection ConstantConditions
        FloatingActionButton mainUpFab = ((FragmentListTabLayout) this.getParentFragment()).mUpFab;
        if (mAdapter == null) {
            mAdapter = new PosterAdapter(this);
        }

        rootView.setPaddingRelative(16, 16, 16, 16);

        //From Tara's answer here: https://stackoverflow.com/questions/2680607/text-with-gradient-in-android
        paintTextView(context, mTvError);
        paintTextView(context, mTvNoFav);

        if (mTabPage == 3) {
            ((MovieListsActivity) getContext()).getFavouritesList();
        }
        //noinspection ConstantConditions
        if (!((MovieListsActivity) getContext()).hasRequestedFromNetwork() && (mTabPage != 3)) {
            hideAndShowView(mTvError, mRvPoster);
        }

        if (mTabPage == 1) {
            mAdapter.setEndListener(handler ->
                    ((MovieListsActivity) getContext()).getMoreTopRated());
        }

        if (mTabPage == 2) {
            mAdapter.setEndListener(handler ->
                    ((MovieListsActivity) getContext()).getMorePopular());
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
        ((MovieListsActivity) getContext()).registerListListeners(this);
        super.onAttach(context);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onDetach() {
        ((FragmentListTabLayout) this.getParentFragment()).unRegisterUpFab(this);
        ((MovieListsActivity) getContext()).unRegisterListListeners(this);
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

        Shader shader = new LinearGradient(0, 0, 0, 45,
                new int[]{colorOne, colorTwo},
                new float[]{0, 1}, Shader.TileMode.REPEAT);
        textView.getPaint().setShader(shader);
    }

    @Override
    public void upFabUp() { mRvPoster.smoothScrollToPosition(0); }

    @Override
    public void resultDelivery(List<Movie> movies, MovieResultType type) {
        if (movies.isEmpty() && type == MovieResultType.FAVOURITES) {
            //Implement add favourites textview
            return;
        }
        if (mTabPage == 1 && type == MovieResultType.TOP_RATED) {
            mAdapter.setMovieData(movies, mAdapter.getItemCount());
        }
        if (mTabPage == 2 && type == MovieResultType.POPULAR) {
            mAdapter.setMovieData(movies, mAdapter.getItemCount());
        }
        if (mTabPage == 3 && type == MovieResultType.FAVOURITES) {
            if (movies.isEmpty()){
                hideAndShowView(mTvNoFav, mRvPoster);
            }
            mAdapter.setMovieData(movies, mAdapter.getItemCount());
        }

    }
}
