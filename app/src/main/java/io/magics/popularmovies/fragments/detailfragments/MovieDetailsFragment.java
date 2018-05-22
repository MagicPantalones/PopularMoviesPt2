package io.magics.popularmovies.fragments.detailfragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.magics.popularmovies.R;
import io.magics.popularmovies.models.Movie;
import io.magics.popularmovies.utils.GlideApp;

import static io.magics.popularmovies.utils.MovieUtils.getOptimalImgSize;
import static io.magics.popularmovies.utils.MovieUtils.posterUrlConverter;


@SuppressWarnings("ConstantConditions")
public class MovieDetailsFragment extends Fragment {

    private static final String ARG_MOVIE = "movie";
    private static final String ARG_IS_FAVOURITE = "isFavourite";
    private static final String ARG_TRANSITION_NAME = "transitionName";

    private Movie mMovie;
    private boolean mIsFavourite;
    private int mTransitionName;

    @BindView(R.id.wrapper_details_main_card)
    CardView mMainCardWrapper;
    @BindView(R.id.fav_fab)
    FloatingActionButton mFavFab;
    @BindView(R.id.fav_fab_anim)
    ImageView mIvFavFabAnim;
    @BindView(R.id.nested_details_container)
    ViewPager mNestedViewPager;
    @BindView(R.id.btn_detail_bar_back)
    ImageView mBtnToolbarBack;
    @BindView(R.id.titles_indicator)
    TabLayout mTitlesIndicator;
    @BindView(R.id.toolbar_detail_fragment)
    Toolbar mToolbar;
    @BindView(R.id.icon_left_hint)
    ImageView mGoLeftHint;
    @BindView(R.id.icon_right_hint)
    ImageView mGoRightHint;

    @Nullable
    @BindView(R.id.pb_details_vote)
    ProgressBar mVoteBar;
    @Nullable
    @BindView(R.id.tv_details_vote)
    TextView mVoteNumber;
    @Nullable
    @BindView(R.id.details_poster_horizontal)
    ImageView mPosterHorizontal;
    @Nullable
    @BindView(R.id.horizontal_poster_wrapper)
    View mWrapperHorizontal;


    private Unbinder mUnbinder;
    private DetailFragInteractionHandler mFragInteractionHandler;

    private MovieDetailsPagerAdapter mPagerAdapter;
    private AnimatedVectorDrawableCompat mAnimatedFabDrawable;

    private int mDefColor;
    private int mFavColor;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    public static MovieDetailsFragment newInstance(Movie movie, boolean isFavourite,
                                                   int transitionName) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        args.putBoolean(ARG_IS_FAVOURITE, isFavourite);
        args.putInt(ARG_TRANSITION_NAME, transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
            mIsFavourite = getArguments().getBoolean(ARG_IS_FAVOURITE);
            mTransitionName = getArguments().getInt(ARG_TRANSITION_NAME);
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail_movie, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        mFavColor = ResourcesCompat.getColor(getContext().getResources(),
                R.color.colorSecondaryAccent, getContext().getTheme());
        mDefColor = ResourcesCompat.getColor(getContext().getResources(),
                R.color.colorPrimaryDark, getContext().getTheme());

        mPagerAdapter = new MovieDetailsPagerAdapter(mMovie,
                mTransitionName + mMovie.getPosterUrl(), getChildFragmentManager());

        prepareSharedElementTransitions();

        if (savedInstanceState == null) {
            postponeEnterTransition();
        }

        return root;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mNestedViewPager.setOffscreenPageLimit(4);
        mNestedViewPager.setAdapter(mPagerAdapter);

        //Sets the visibility of the arrows on the sides of the nested CardView
        mNestedViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        mGoLeftHint.setVisibility(View.INVISIBLE);
                        mGoRightHint.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        mGoLeftHint.setVisibility(View.VISIBLE);
                        mGoRightHint.setVisibility(View.INVISIBLE);
                        break;
                    default:
                        mGoLeftHint.setVisibility(View.VISIBLE);
                        mGoRightHint.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        //Sets the behaviour of the arrows
        mGoRightHint.setOnClickListener(v -> {
            if (mGoRightHint.getVisibility() == View.VISIBLE) {
                mNestedViewPager.setCurrentItem(mNestedViewPager.getCurrentItem() + 1, true);
            }
        });

        mGoLeftHint.setOnClickListener(v -> {
            if (mGoLeftHint.getVisibility() == View.VISIBLE) {
                mNestedViewPager.setCurrentItem(mNestedViewPager.getCurrentItem() - 1, true);
            }
        });

        //Found that a dotted ViewPager indicator gave the best visual result.
        //Tried a lot of different indicators.
        mTitlesIndicator.setupWithViewPager(mNestedViewPager, true);

        mBtnToolbarBack.setOnClickListener(v -> getActivity().onBackPressed());

        //Notifies the listener in the Activity so it adds or removes a movie from the SQLite database.
        //Then animates the button.
        mFavFab.setOnClickListener(v -> {
            mFragInteractionHandler.favFabClicked(mMovie, mIsFavourite);
            mIsFavourite = !mIsFavourite;
            fabAnim();
        });

        //Initializes the correct methods based on the layout configuration.
        //The detail fragment and the nested ViewPager contains different Views based on the screen orientation.
        if (getContext().getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE) {
            initLandscapeLayout();
        } else {
            initPortraitLayout();
        }

        Handler fabIntroHandler = new Handler();

        fabIntroHandler.postDelayed(() -> {
            if (mFavFab != null) {
                mFavFab.setBackgroundTintList(ColorStateList.valueOf(mIsFavourite ?
                        mFavColor : mDefColor));
                mFavFab.show();
            }
        }, 400);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DetailFragInteractionHandler) {
            mFragInteractionHandler = (DetailFragInteractionHandler) context;
        }

    }

    @Override
    public void onDestroyView() {
        if (mAnimatedFabDrawable != null && mAnimatedFabDrawable.isRunning()) {
            mAnimatedFabDrawable.stop();
        }
        mUnbinder.unbind();
        super.onDestroyView();
    }

    //The ViewPager needs to be in the position of the poster, or the shared transition won't work.
    public void prepareToGetPopped() {
        if (mTitlesIndicator.getSelectedTabPosition() != 0) {
            mNestedViewPager.setCurrentItem(0, false);
        }
    }

    public int getParentListType(){ return mTransitionName; }

    /**
     * Based on the value in {@link #mIsFavourite} sets the correct AnimatedVectorDrawable.
     * Tried to make the app have a MinSdk of 19. But since I had made this project way bigger than
     * expected, I used a MinSdk of 21 to avoid a lot of SDK checks.
     * <br></br>
     * I wanted to animate the background of the FAB as well as the icon, so i chose to hide the FAB
     * and have a seperate ImageView with the animation instead.
     */
    private void fabAnim() {

        mAnimatedFabDrawable = AnimatedVectorDrawableCompat.create(getContext(), mIsFavourite ?
                R.drawable.ic_anim_heart_to_fav : R.drawable.ic_anim_heart_from_fav);

        mIvFavFabAnim.setImageDrawable(mAnimatedFabDrawable);

        if (mAnimatedFabDrawable != null) {
            mAnimatedFabDrawable.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    mAnimatedFabDrawable.unregisterAnimationCallback(this);

                    mFavFab.setBackgroundTintList(ColorStateList.valueOf(mIsFavourite ?
                            mFavColor : mDefColor));

                    mIvFavFabAnim.setImageDrawable(null);

                    mFavFab.setVisibility(View.VISIBLE);

                    super.onAnimationEnd(drawable);
                }
            });

            mFavFab.setVisibility(View.INVISIBLE);
            mIvFavFabAnim.setVisibility(View.VISIBLE);
            mAnimatedFabDrawable.start();
        }


    }


    private void prepareSharedElementTransitions() {
        TransitionSet sharedTransition = (TransitionSet) TransitionInflater.from(getContext())
                .inflateTransition(R.transition.card_enter_transition);

        setSharedElementEnterTransition(sharedTransition);

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                Fragment posterFrag = (Fragment) mPagerAdapter.instantiateItem(mNestedViewPager, 0);

                View view = posterFrag.getView();

                boolean landscapeOrientation = getContext().getResources()
                        .getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

                if (view == null) {
                    return;
                }

                sharedElements.put(names.get(0), landscapeOrientation ?
                        mWrapperHorizontal : view.findViewById(R.id.nested_poster_wrapper));
                sharedElements.put(names.get(1), landscapeOrientation ?
                        mPosterHorizontal : view.findViewById(R.id.iv_poster_details));
                sharedElements.put(names.get(2), mToolbar);
            }
        });

    }

    /**
     * When the configuration is in portrait, the majority of the information gets shown in the
     * nested ViewPager. But the {@link #mVoteBar} and {@link #mVoteNumber} views are in the
     * viewPager when the config is Landscape
     */
    private void initPortraitLayout() {
        mVoteBar.setProgress((int) (mMovie.getVoteAverage() * 10));
        mVoteNumber.setText(String.valueOf(mMovie.getVoteAverage()));
    }


    /**
     * Initiates and prepares the fragment to be laid out in Landscape orientation.
     *
     * The movie poster's width is set based on the height of the poster, which is set to
     * MATCH_PARENT in the layout XML. Therefore the {@link #mPosterHorizontal}'s width gets
     * set, and the postponed transition gets started,
     * after the layout has been measured in {@link Target#getSize}.
     */
    private void initLandscapeLayout() {

        mWrapperHorizontal.setTransitionName(mTransitionName + mMovie.getPosterUrl());

        mPosterHorizontal.setTransitionName("poster" + mTransitionName + mMovie.getPosterUrl());
        mPosterHorizontal.setContentDescription(mMovie.getTitle());

        GlideApp.with(this)
                .load(posterUrlConverter(getOptimalImgSize(getContext()), mMovie.getPosterUrl()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontTransform()
                .override(Target.SIZE_ORIGINAL)
                .into(mPosterHorizontal)
                .getSize((width, height) -> {
                    mPosterHorizontal.setMinimumWidth(height / 3 * 2);
                    startPostponedEnterTransition();
                });
    }


    public interface DetailFragInteractionHandler {
        void favFabClicked(Movie movie, Boolean isFavourite);
    }

}
