package io.magics.popularmovies;

import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.facebook.stetho.Stetho;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.magics.popularmovies.models.Movie;

public class MovieListsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_lists);
        ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);

        if (savedInstanceState == null) {
            FragmentListTabLayout frag = FragmentListTabLayout.instantiateFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container_main, frag).commit();
        }
    }



    public void startFrag(Movie movie, boolean check){
        MovieDetailsFragment frag = MovieDetailsFragment.newInstance(movie, check);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container_main, frag);
        ft.addToBackStack(null);
        ft.commit();
    }

}
